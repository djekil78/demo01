package ru.abcconsulting.bio.integration.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.abcconsulting.bio.integration.dto.*;
import ru.abcconsulting.bio.integration.entity.integration.ExtendedBioSmartLog;
import ru.abcconsulting.bio.integration.entity.integration.QExtendedBioSmartLog;
import ru.abcconsulting.bio.integration.entity.wfm.RecordOrgUnit;
import ru.abcconsulting.bio.integration.entity.wfm.WfmRecord;
import ru.abcconsulting.bio.integration.repository.integration.ExtendedBioSmartLogRepository;
import ru.abcconsulting.bio.integration.repository.wfm.RecordOrgUnitRepository;
import ru.abcconsulting.bio.integration.repository.wfm.WfmRecordRepository;
import ru.abcconsulting.bio.integration.util.BioSmartEvents;
import ru.abcconsulting.bio.integration.util.Links;
import ru.abcconsulting.bio.integration.util.RecognitionPurpose;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class BioSmartIntegrationRecordsService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Links links;
    @Autowired
    private BioSmartIntegrationOrgUnitsAndEmployeesService bioSmartIntegrationOrgUnitsAndEmployeesService;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private RecordOrgUnitRepository recordOrgUnitRepository;
    @Autowired
    private WfmRecordRepository wfmRecordRepository;
    @Autowired
    private ExtendedBioSmartLogRepository extendedBioSmartLogRepository;

    @Scheduled(cron = "${integration.biosmart.records.cron}")
    public void integrationByScheduler() {
        integrationRecords();
    }

    public void integrationRecords() {
        log.info("Integration of records BIO-SMART->WFM starts!");

        String token = getToken(links.LOGIN, links.PASSWORD);

        getRecordsFromBioSmartAndInsertIntoDb(token);

        List<BioSmartEmployeeDto> employees = bioSmartIntegrationOrgUnitsAndEmployeesService.getAllEmployees(token);
        List<BioSmartOrgUnitDto> orgUnits = bioSmartIntegrationOrgUnitsAndEmployeesService.getAllOrgUnits(token);
        List<ExtendedBioSmartLog> logs = getAllLogsWhereIntegratedIsFalse();

        List<CompoundLogDto> compoundLogs = joinLogEmployeeOrgUnit(logs, employees, orgUnits);

        // сохраняем изменения в БД  для каждой записи
        insertRecordsInHrPortal(compoundLogs);

        log.info("Integration of records BIO-SMART->WFM finished!");
    }

    public void insertRecordsInHrPortal(List<CompoundLogDto> compoundLogs) {
        log.info("Insert records DB->WFM starts!");
        for (CompoundLogDto log : compoundLogs) {

            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);

            try {
                // вставляем запись в портал
                WfmRecord rec = new WfmRecord().setTimestamp(LocalDateTime.parse(log.getTime())).setZoneOffset(log.getTimezone())
                        .setPurpose(log.getEvent().intValue()).setEmployeeId(log.getHrportalEmployeeId());
                rec = wfmRecordRepository.save(rec);

                //  сохраняем в hrportal.record_organizationunit с новым hr.record.id
                final RecordOrgUnit rOrg = new RecordOrgUnit().setRecordId(rec.getId()).setOrgUnitId(log.getHrportalOrgUnitId());
                recordOrgUnitRepository.save(rOrg);

                //  апдейтим поле  bio_integration.record.integrated
                final ExtendedBioSmartLog extLog = extendedBioSmartLogRepository.getOne(log.getId());
                extLog.setIntegrated(true);
                extendedBioSmartLogRepository.save(extLog);

                transactionManager.commit(transaction);
            } catch (Exception ex) {
                ex.printStackTrace();
                transactionManager.rollback(transaction);
            }
        }
        log.info("Insert records DB->WFM finished!");
    }

    public void getRecordsFromBioSmartAndInsertIntoDb(String token) {
        log.info("Insert records BIO-SMART->DB starts!");
        List<ExtendedBioSmartLogDto> extendedBioSmartLogs = new LinkedList<>();
        Long maxLogId = getMaxLogId();
        List<BioSmartLogDto> logDtos = getAllLogs(token, maxLogId);
        logDtos.forEach(logDto -> {
            ExtendedBioSmartLogDto extendedBioSmartLogDto = logDto.toExtendedBioSmartLogDto();

            extendedBioSmartLogDto.setTime(convertDate(logDto.getTime()));
            extendedBioSmartLogDto.setEvent(mapEvents(logDto.getEvent()));
            if (logDto.getObject_tz() == null) {
                extendedBioSmartLogDto.setObject_tz(links.defaultTimeZone);
            }
            // timezone -> zoneOffset на этапе вставки в hrportal

            extendedBioSmartLogs.add(extendedBioSmartLogDto);
        });
        try {
            insertRecords(extendedBioSmartLogs);
        } catch (Exception ex) {
            log.error("Cannot INSERT records into DB: " + ex.getMessage());
        }
        log.info("Insert records BIO-SMART->DB finished!");
    }

    public List<CompoundLogDto> joinLogEmployeeOrgUnit(List<ExtendedBioSmartLog> logs,
                                                       List<BioSmartEmployeeDto> employeeDtos,
                                                       List<BioSmartOrgUnitDto> orgUnitDtos) {

        List<CompoundLogDto> compoundLogs = new ArrayList<>();

        for (ExtendedBioSmartLog log : logs) {
            BioSmartEmployeeDto bioSmartEmployeeDto = searchInEmployees(employeeDtos, Objects.requireNonNull(log.getSubjectId()));

            if (bioSmartEmployeeDto != null) {
                BioSmartOrgUnitDto bioSmartOrgUnitDto = searchInOrgUnits(orgUnitDtos, Objects.requireNonNull(bioSmartEmployeeDto.getParent()));

                if (bioSmartOrgUnitDto != null) {
                    String hrportalOrgUnitId = bioSmartOrgUnitDto.getDepnum();
                    String hrportalEmployeeId = bioSmartEmployeeDto.getWorkernum();

                    if (hrportalEmployeeId != null && hrportalOrgUnitId != null &&
                            !hrportalEmployeeId.equals("0") && !hrportalOrgUnitId.equals("0") &&
                            !hrportalEmployeeId.equals("") && !hrportalOrgUnitId.equals("")) {
                        try {
                            Long employeeId = Long.parseLong(hrportalEmployeeId);
                            Long orgUnitId = Long.parseLong(hrportalOrgUnitId);
                            compoundLogs.add(new CompoundLogDto(log.getId(), employeeId, orgUnitId, log.getEvent(), log.getTime(), log.getObjectTz()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
        return compoundLogs;
    }

    private BioSmartEmployeeDto searchInEmployees(List<BioSmartEmployeeDto> bioSmartEmployeeDtos, Long bioSmartEmployeeId) {
        return bioSmartEmployeeDtos
                .stream()
                .filter(employeeDto -> employeeDto.getId().equals(bioSmartEmployeeId))
                .findAny()
                .orElse(null);
    }

    private BioSmartOrgUnitDto searchInOrgUnits(List<BioSmartOrgUnitDto> bioSmartOrgUnitDtos, Long bioSmartOrgUnitId) {
        return bioSmartOrgUnitDtos
                .stream()
                .filter(orgUnitDto -> orgUnitDto.getId().equals(bioSmartOrgUnitId))
                .findAny()
                .orElse(null);
    }

    public String getToken(String login, String password) {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setBasicAuth(login, password);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        final ResponseEntity<BioSmartAuthDto> response = restTemplate.postForEntity(links.URL_BIO_SMART + links.URL_LOGIN, request, BioSmartAuthDto.class);
        return Objects.requireNonNull(response.getBody()).getToken();
    }

    public List<BioSmartLogDto> getAllLogs(String token, Long maxId) {
        List<BioSmartLogDto> logDtos = new LinkedList<>();
        // GET Logs
        HttpEntity entity = createHttpEntity(token);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(links.URL_BIO_SMART + links.URL_LOGS)
                .queryParam("ordering", "id")
                .queryParam("event__in", BioSmartEvents.getAllEventIdsInString());

        if (maxId != null) {
            builder.queryParam("id__gt", maxId);
        }

        try {
            ResponseEntity<List<BioSmartLogDto>> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, new ParameterizedTypeReference<List<BioSmartLogDto>>() {
                    });

            logDtos.addAll(Objects.requireNonNull(response.getBody()));

        } catch (RestClientException ex) {
            log.error("Cannot parse response to List<BioSmartLogDto>");
            log.info("Sending request by page start!");

            Long pageNum = 1L;
            builder
                    .queryParam("page_size", links.LOGS_MAX_PAGE_SIZE)
                    .queryParam("page", pageNum);

            while (true) {
                builder.replaceQueryParam("page", pageNum);

                ResponseEntity<BioSmartPageLogDto> responseEntity = restTemplate.exchange(
                        builder.toUriString(), HttpMethod.GET, entity, new ParameterizedTypeReference<BioSmartPageLogDto>() {
                        });

                logDtos.addAll(Objects.requireNonNull(responseEntity.getBody()).getResults());

                pageNum++;

                if (responseEntity.getBody().getNext() == null) {
                    break;
                }
            }
            log.info("Sending request by page finished!");
        }
        return logDtos;
    }

    // helpers

    private HttpHeaders setHeaders(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.set("Authorization", "Token " + token);
        return httpHeaders;
    }

    private HttpEntity createHttpEntity(String token) {
        HttpHeaders httpHeaders = setHeaders(token);
        return new HttpEntity(httpHeaders);
    }

    private String convertDate(String date) {
        Instant instant = Instant.parse(date);
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(instant.toEpochMilli());
    }

    @Transactional(readOnly = true)
    public Long getMaxLogId() {
        final Long maxId = extendedBioSmartLogRepository.getMaxOuterId();
        return maxId == 0 ? null : maxId;
    }

    @Transactional(transactionManager = "bioIntegrationTransactionManager")
    public void insertRecords(List<ExtendedBioSmartLogDto> logList) {
        for (ExtendedBioSmartLogDto eLog : logList) {
            final ExtendedBioSmartLog lg = new ExtendedBioSmartLog().setOuterId(eLog.getOuterId()).setObjectId(eLog.getObject_id())
                    .setSubjectId(eLog.getSubject_id()).setEvent(eLog.getEvent()).setTime(eLog.getTime()).setObjectTz(eLog.getObject_tz())
                    .setIntegrated(eLog.isIntegrated());
            extendedBioSmartLogRepository.save(lg);
        }
    }

    @Transactional(readOnly = true)
    public List<ExtendedBioSmartLog> getAllLogsWhereIntegratedIsFalse() {
        QExtendedBioSmartLog l = QExtendedBioSmartLog.extendedBioSmartLog;
        return (List<ExtendedBioSmartLog>) extendedBioSmartLogRepository.findAll(l.integrated.isFalse());
    }

    private Long mapEvents(Long event) {
        Long result = null;

        if (event.equals(BioSmartEvents.EMPLOYEE_ENTRY.getEventId()) ||
                event.equals(BioSmartEvents.EMPLOYEE_ENTRY_BY_CARD.getEventId()) ||
                event.equals(BioSmartEvents.EMPLOYEE_ENTRY_BY_PIN.getEventId()) ||
                event.equals(BioSmartEvents.EMPLOYEE_ENTRY_MANUAL_INPUT.getEventId())) {
            RecognitionPurpose purpose = RecognitionPurpose.OPEN_SHIFT;
            result = (long) purpose.ordinal();
        } else if (event.equals(BioSmartEvents.EMPLOYEE_EXIT.getEventId()) ||
                event.equals(BioSmartEvents.EMPLOYEE_EXIT_BY_CARD.getEventId()) ||
                event.equals(BioSmartEvents.EMPLOYEE_EXIT_BY_PIN.getEventId()) ||
                event.equals(BioSmartEvents.EMPLOYEE_EXIT_MANUAL_INPUT.getEventId())
        ) {
            RecognitionPurpose purpose = RecognitionPurpose.CLOSE_SHIFT;
            result = (long) purpose.ordinal();
        } else if (event.equals(BioSmartEvents.LUNCH_OUT.getEventId()) ||
                event.equals(BioSmartEvents.LUNCH_OUT_BY_CARD.getEventId()) ||
                event.equals(BioSmartEvents.LUNCH_OUT_BY_PIN.getEventId())
        ) {
            RecognitionPurpose purpose = RecognitionPurpose.OPEN_BREAK;
            result = (long) purpose.ordinal();
        } else if (event.equals(BioSmartEvents.ENTRANCE_FROM_LUNCH.getEventId()) ||
                event.equals(BioSmartEvents.ENTRANCE_FROM_LUNCH_BY_CARD.getEventId()) ||
                event.equals(BioSmartEvents.ENTRANCE_FROM_LUNCH_BY_PIN.getEventId())
        ) {
            RecognitionPurpose purpose = RecognitionPurpose.CLOSE_BREAK;
            result = (long) purpose.ordinal();
        }

        return result;
    }
}
