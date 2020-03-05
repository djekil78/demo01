package ru.abcconsulting.bio.integration.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.abcconsulting.bio.integration.entity.bio.*;
import ru.abcconsulting.bio.integration.entity.wfm.RecordOrgUnit;
import ru.abcconsulting.bio.integration.entity.wfm.WfmRecord;
import ru.abcconsulting.bio.integration.repository.bio.*;
import ru.abcconsulting.bio.integration.repository.wfm.RecordOrgUnitRepository;
import ru.abcconsulting.bio.integration.repository.wfm.WfmRecordRepository;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationRecordsService {

    private final PlatformTransactionManager transactionManager;
    private final BioRecordRepository bioRecordRepository;
    private final WfmRecordRepository wfmRecordRepository;
    private final RecordOrgUnitRepository recordOrgUnitRepository;
    private final BioPersonRepository bioPersonRepository;
    private final BioPersonGroupRepository bioPersonGroupRepository;
    private final PersonGroupPersonRepository personGroupPersonRepository;

    @Value("${integration.bio.demo:false}")
    public Boolean isDemo;

    @Scheduled(cron = "${integrationrecord.cron}")
    public void integrationByScheduler() {
        List<BioPerson> bioPersons = bioPersonRepository.findAll();
        List<BioPersonGroup> personGroups = bioPersonGroupRepository.findAll();
        List<PersonGroupPerson> personGroupPersons = personGroupPersonRepository.findAll();
        integrationRecords(bioPersons, personGroups, personGroupPersons);
    }

    private void integrationRecords(List<BioPerson> persons,
                                    List<BioPersonGroup> personGroups,
                                    List<PersonGroupPerson> personGroupPersons) {
        log.info("Integration of records BIO->WFM starts!");
        // сохраняем весь массив записей для интеграции - выборка из БД
        List<BioRecord> bioRecords = getBioVRecordsPersonGroups();

        // сохраняем отдельно массив bio.record.id
        Set<Long> bioRecordsId = bioRecords.stream().map(BioRecord::getId).collect(Collectors.toSet());

        // Map<personId, outerId> сотрудники
        Map<Long, Long> bioPersonOuterIdMapById = new HashMap<>();
        persons.forEach(v -> {
            try {
                Long outerId = Long.parseLong(v.getOuterId());
                bioPersonOuterIdMapById.put(v.getId(), outerId);
            } catch (NumberFormatException ignored) {
            }
        });

        // Map<personId, outerId> оргюниты
        Map<Long, Long> bioPersonGroupOuterIdMapById = new HashMap<>();
        personGroups.forEach(v -> {
            try {
                Long outerId = Long.parseLong(v.getOuterId());
                bioPersonGroupOuterIdMapById.put(v.getId(), outerId);
            } catch (NumberFormatException ignored) {
            }
        });

        // Map<personId, List<PersonGroupOuterId>>
        Map<Long, List<Long>> bioPersonGroupOuterIdMapByPersonOuterId = new HashMap<>();
        personGroupPersons.forEach(v -> {
            Long personId = v.getPersonId();
            Long personGroupId = v.getPersonGroupId();
            Long personOuterId = bioPersonOuterIdMapById.getOrDefault(personId, null);
            if (personOuterId != null) {
                if (bioPersonGroupOuterIdMapByPersonOuterId.containsKey(personOuterId)) {
                    if (bioPersonGroupOuterIdMapById.containsKey(personGroupId)) {
                        bioPersonGroupOuterIdMapByPersonOuterId.get(personOuterId).add(bioPersonGroupOuterIdMapById.get(personGroupId));
                    }
                } else {
                    List<Long> personGroupOuterIds = new ArrayList<>();
                    if (bioPersonGroupOuterIdMapById.containsKey(personGroupId)) {
                        personGroupOuterIds.add(bioPersonGroupOuterIdMapById.get(personGroupId));
                    }
                    bioPersonGroupOuterIdMapByPersonOuterId.put(personOuterId, personGroupOuterIds);
                }
            }
        });

        Map<WfmRecord, List<Long>> bioRecordMapByRecord = new HashMap<>();

        for (Long recordId : bioRecordsId) {
            bioRecords.stream().filter(x -> x.getId().equals(recordId)).forEach(v -> {
                try {
                    if (v.getZoneOffset() == null) {
                        Integer serverZoneOffset = (TimeZone.getDefault().getRawOffset() / 1000);
                        v.setZoneOffset(serverZoneOffset);
                    }
                    final String zoneId = getZoneId(v.getZoneOffset());

                    WfmRecord hrWfmRecord = new WfmRecord(v.getId()).setEmployeeId(Long.parseLong(v.getPersonId())).setZoneOffset(zoneId).setPurpose(v.getPurpose())
                            .setTimestamp(convertToDateTime(v.getTimestamp(), zoneId));
                    log.info("old convert date {}", getSqlDateWithTimeZone(v.getTimestamp(), zoneId));

                    List<Long> personGroupIds = new ArrayList<>();
                    if (v.getPersonGroups() == null || v.getPersonGroups().size() == 0) { // Если в биометрии нет данных по оргюниту к которым относится распознанный сотрудник, в соответствии
                        // с распознавшим терминалом, то по умолчанию проставляем ему оргюниты к которым он сейчас относится в текущий момент
                        Long personOuterId = Long.parseLong(v.getPersonId());
                        if (bioPersonGroupOuterIdMapByPersonOuterId.containsKey(personOuterId)) {
                            personGroupIds.addAll(bioPersonGroupOuterIdMapByPersonOuterId.get(personOuterId));
                        }
                    } else {
                        personGroupIds.addAll(v.getPersonGroups().stream().map(e -> Long.parseLong(e)).collect(Collectors.toSet()));
                    }
                    bioRecordMapByRecord.put(hrWfmRecord, personGroupIds);
                } catch (NumberFormatException ex) {
                    log.error(ex.getMessage());
                }
            });
        }

        bioRecordMapByRecord.forEach((k, v) -> {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
            try {
                final WfmRecord savedRec = wfmRecordRepository.save(k);// вставляем запись в портал

                //  сохраняем в hrportal.record_organizationunit с новым hr.record.id
                Long recordId = savedRec.getId();
                if (recordOrgUnitRepository.countByRecordId(recordId) > 0) {
                    recordOrgUnitRepository.deleteByRecordId(recordId); // очищаем связки с оргюнитами на случай возникновения коллизий, так как id таблицы record в WFM сейчас берется из bio (id таблицы record)
                }
                for (Long pg : v) {
                    final RecordOrgUnit rOrg = new RecordOrgUnit().setOrgUnitId(pg).setRecordId(savedRec.getId());
                    recordOrgUnitRepository.save(rOrg);
                }

                //  апдейтим поле  biometry.record.integrated
                final Optional<BioRecord> byId = bioRecordRepository.findById(k.getId());
                if (byId.isPresent()) {
                    final BioRecord bioRecord = byId.get();
                    bioRecord.setIntegrated(true);
                    bioRecordRepository.save(bioRecord);
                }
                transactionManager.commit(transaction);
            } catch (Exception e) {
                transactionManager.rollback(transaction);
            }

        });
        log.info("Integration of records BIO->WFM finished!");

    }

    private String getZoneId(Integer oneOffsetInt) {
        if (oneOffsetInt == null) {
            return null;
        }
        return oneOffsetInt == 0 ? "GMT" : "Etc/GMT" + (oneOffsetInt > 0 ? "-" + oneOffsetInt / 3600 : "+" + Math.abs(oneOffsetInt) / 3600);
    }

    private String getSqlDateWithTimeZone(Long timestamp, String zoneId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timestamp));
        if (isDemo) {
            calendar.add(Calendar.DATE, -1); // смещаем день на 1 назад в случае демо режима
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        sdf.setTimeZone(TimeZone.getTimeZone(zoneId)); // переводим время в локальное в соответствии с настройками терминала
        return sdf.format(calendar.getTime());
    }

    private LocalDateTime convertToDateTime(Long timestamp, String zoneId) {
        LocalDateTime rez = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of(zoneId));
        if (isDemo) {
            rez = rez.plusDays(-1); // смещаем день на 1 назад в случае демо режима
        }
        return rez;
    }

    // helpers
    @Transactional(readOnly = true)
    public List<BioRecord> getBioVRecordsPersonGroups() {
        return bioRecordRepository.findBioRecordByErrorIsNullAndAliveIsTrueAndPersonIdIsNotNullAndIntegratedIsFalse();
    }
}
