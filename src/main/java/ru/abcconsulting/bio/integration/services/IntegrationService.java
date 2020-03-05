package ru.abcconsulting.bio.integration.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.sql.Date;

import java.util.*;
import java.util.stream.Collectors;


import ru.abcconsulting.bio.integration.dto.BioFaceDescriptorDto;
import ru.abcconsulting.bio.integration.dto.BioSmartLogDto;
import ru.abcconsulting.bio.integration.entity.bio.*;
import ru.abcconsulting.bio.integration.entity.wfm.Employee;
import ru.abcconsulting.bio.integration.entity.wfm.EmployeePosition;
import ru.abcconsulting.bio.integration.entity.wfm.OrgUnit;
import ru.abcconsulting.bio.integration.entity.wfm.Position;
import ru.abcconsulting.bio.integration.repository.bio.BioPersonGroupRepository;
import ru.abcconsulting.bio.integration.repository.bio.BioPersonRepository;
import ru.abcconsulting.bio.integration.repository.bio.PersonGroupPersonRepository;
import ru.abcconsulting.bio.integration.repository.wfm.EmployeePositionRepository;
import ru.abcconsulting.bio.integration.repository.wfm.EmployeeRepository;
import ru.abcconsulting.bio.integration.repository.wfm.OrgUnitRepository;
import ru.abcconsulting.bio.integration.repository.wfm.PositionRepository;
import ru.abcconsulting.bio.integration.util.Links;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final EmployeeRepository employeeRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final EmployeePositionRepository employeePositionRepository;
    private final PositionRepository positionRepository;

    private final BioPersonRepository bioPersonRepository;
    private final BioPersonGroupRepository bioPersonGroupRepository;
    private final PersonGroupPersonRepository personGroupPersonRepository;

    @Value("${integration.bio.deleteDescriptors:false}")
    public boolean integrationBioDeleteDescriptors;

    private final RestTemplate restTemplate;
    private final Links links;

    @Scheduled(cron = "${integration.cron}")
    public void integrationByScheduler() {
        List<BioPerson> bioPersons = bioPersonRepository.findAll();
        final List<Employee> employees = employeeRepository.findAll();
        final List<EmployeePosition> employeePositions = employeePositionRepository.findAll();
        integratePerson(bioPersons, employees, employeePositions);

        List<BioPersonGroup> personGroups = bioPersonGroupRepository.findAll();
        final List<OrgUnit> orgUnits = orgUnitRepository.findAll();
        integratePersonGroup(personGroups, orgUnits);

        final List<PersonGroupPerson> personGroupPersons = personGroupPersonRepository.findAll();
        bioPersons = bioPersonRepository.findAll();
        personGroups = bioPersonGroupRepository.findAll();
        final List<Position> positions = positionRepository.findAll();
        integratePersonGroupPerson(personGroupPersons, bioPersons,
                personGroups, positions, employeePositions, employees);
    }

    @Transactional
    public void integratePerson(List<BioPerson> bioPersons,
                                List<Employee> employees,
                                List<EmployeePosition> employeePositions) {
        log.info("Integration of employees WFM->BIO starts!");

        Map<String, BioPerson> bioPersonMapByOuterId = new HashMap<>();
        bioPersons.forEach(v -> bioPersonMapByOuterId.put(v.getOuterId(), v));

        Map<Long, Employee> hrEmployeeMapById = new HashMap<>();
        //добавляем только те записи, где endDate = null
        Date currentDate = Date.valueOf(LocalDate.now());
        employees.forEach(v -> {
            if (v.getEndWorkDate() == null || currentDate.before(v.getEndWorkDate()) || currentDate.equals(v.getEndWorkDate())) {
                hrEmployeeMapById.put(v.getId(), v);
            }
        });

        Map<Long, EmployeePosition> hrEmployeePositionMapByEmployeeId = new HashMap<>();
        employeePositions.forEach(v -> {
            if (v.getEndDate() == null || currentDate.before(v.getEndDate()) || currentDate.equals(v.getEndDate())) {
                hrEmployeePositionMapByEmployeeId.put(v.getEmployeeId(), v);
            }
        });


        for (Employee hr : employees) {
            if (!bioPersonMapByOuterId.containsKey(hr.getId().toString())) {
                boolean active = false;
                if (hrEmployeeMapById.containsKey(hr.getId()) && hrEmployeePositionMapByEmployeeId.containsKey(hr.getId())) {
                    active = true;
                }

                BioPerson p = new BioPerson().setOuterId(hr.getId().toString()).setActive(active).setExternal(true)
                        .setFirstName(hr.getFirstName()).setPatronymicName(hr.getPatronymicName()).setLastName(hr.getLastName());
                bioPersonRepository.save(p);
            }

            if (bioPersonMapByOuterId.containsKey(hr.getId().toString())) {
                BioPerson bp = bioPersonMapByOuterId.get(hr.getId().toString());

                if ((hr.getFirstName() != null && !hr.getFirstName().equals(bp.getFirstName())) ||
                        (hr.getLastName() != null && !hr.getLastName().equals(bp.getLastName())) ||
                        (hr.getPatronymicName() != null && !hr.getPatronymicName().equals(bp.getPatronymicName()))) {


                    bp.setFirstName(hr.getFirstName()).setPatronymicName(hr.getPatronymicName()).setLastName(hr.getLastName());
                    bioPersonRepository.save(bp);
                }
            }
        }
        log.info("Integration of employees WFM->BIO finished!");
    }

    @Transactional
    public void integratePersonGroup(List<BioPersonGroup> personGroups,
                                     List<OrgUnit> orgUnits) {
        log.info("Integration of orgunits WFM->BIO starts!");
        Map<String, BioPersonGroup> bioPersonGroupMapByOuterId = new HashMap<>();
        personGroups.forEach(v -> bioPersonGroupMapByOuterId.put(v.getOuterId(), v));

        //Map<String, PersonDto> bioPersonMapByOuterId = new HashMap<>();
        //refreshAndGetBioPersons().forEach(v -> bioPersonMapByOuterId.put(v.getOuterId(), v));

        for (OrgUnit hr : orgUnits) {
            if (!bioPersonGroupMapByOuterId.containsKey(hr.getId().toString())) {
                final BioPersonGroup pg = new BioPersonGroup().setOuterId(hr.getId().toString()).setExternal(true).setName(hr.getName());
                bioPersonGroupRepository.save(pg);
            }
        }

        personGroups.forEach(v -> bioPersonGroupMapByOuterId.put(v.getOuterId(), v));
        for (OrgUnit hr : orgUnits) {
            if (hr.getParentId() != null) {
                QBioPersonGroup qbpg = QBioPersonGroup.bioPersonGroup;
                final Optional<BioPersonGroup> optional = bioPersonGroupRepository.findOne(qbpg.outerId.eq(hr.getId().toString()));
                if (optional.isPresent()) {
                    final Long parentId = bioPersonGroupMapByOuterId.containsKey(hr.getParentId().toString()) ?
                            bioPersonGroupMapByOuterId.get(hr.getParentId().toString()).getId() : null;
                    final BioPersonGroup pg = optional.get();
                    pg.setParentId(parentId);
                    bioPersonGroupRepository.save(pg);
                }
            }
        }
        log.info("Integration of orgunits WFM->BIO finished!");
    }

    @Transactional
    public void integratePersonGroupPerson(List<PersonGroupPerson> personGroupPersons,
                                           List<BioPerson> persons,
                                           List<BioPersonGroup> personGroups,
                                           List<Position> positions,
                                           List<EmployeePosition> employeePositions,
                                           List<Employee> employees) {
        log.info("Integration of positions WFM->BIO starts!");
        // Формируем 2 мэпы уже интегрированных должностей <personId, List<personGroupId>> и <personId, Map<personGroupId, positionName>>
        Map<Long, List<Long>> bioPersonGroupPersonPersonGroupIdsMapByPersonId = new HashMap<>();
        Map<Long, Map<Long, String>> bioPersonGroupPersonPersonIdsAndPositionNamesMapByPersonId = new HashMap<>();
        personGroupPersons.forEach(v -> {
            Long personId = v.getPersonId();
            if (bioPersonGroupPersonPersonGroupIdsMapByPersonId.containsKey(personId)) {
                bioPersonGroupPersonPersonGroupIdsMapByPersonId.get(personId).add(v.getPersonGroupId());
            } else {
                List<Long> dtos = new ArrayList<>();
                dtos.add(v.getPersonGroupId());
                bioPersonGroupPersonPersonGroupIdsMapByPersonId.put(personId, dtos);
            }
            if (bioPersonGroupPersonPersonIdsAndPositionNamesMapByPersonId.containsKey(personId)) {
                bioPersonGroupPersonPersonIdsAndPositionNamesMapByPersonId.get(personId).put(v.getPersonGroupId(), v.getPositionName());

            } else {
                Map<Long, String> dtos = new HashMap<>();
                dtos.put(v.getPersonGroupId(), v.getPositionName());
                bioPersonGroupPersonPersonIdsAndPositionNamesMapByPersonId.put(personId, dtos);
            }
        });

        // Map<outerId, personDto>
        Map<String, BioPerson> bioPersonMapByOuterId = new HashMap<>();
        persons.forEach(v -> bioPersonMapByOuterId.put(v.getOuterId(), v));

        // Map<outerId, personGroupDto>
        Map<String, BioPersonGroup> bioPersonGroupMapByOuterId = new HashMap<>();
        personGroups.forEach(v -> bioPersonGroupMapByOuterId.put(v.getOuterId(), v));

        // Map<positionId, positionDto>
        Map<Long, Position> hrPositionMapById = new HashMap<>();
        positions.forEach(v -> hrPositionMapById.put(v.getId(), v));

        // Map<orgUnitId, orgUnitDto>
//        refreshAndGetHrOrgUnits();

        // Map<employeeId, EmployeePositionDto> - только текущие должности на которых работает сотрудник
        Map<Long, List<EmployeePosition>> hrEmployeePositionMapByEmployeeId = new HashMap<>();
        Date currentDate = Date.valueOf(LocalDate.now());
        employeePositions.forEach(v -> {
            Long employeeId = v.getEmployeeId();
            if (v.getEndDate() == null || currentDate.before(v.getEndDate()) || currentDate.equals(v.getEndDate())) { //добавляем только те записи, где endDate = null или текущая дата раньше даты увольнения
                if (hrEmployeePositionMapByEmployeeId.containsKey(employeeId)) {
                    hrEmployeePositionMapByEmployeeId.get(employeeId).add(v);
                } else {
                    List<EmployeePosition> dtos = new ArrayList<>();
                    dtos.add(v);
                    hrEmployeePositionMapByEmployeeId.put(employeeId, dtos);
                }
            }
        });

        // Проходимся по всем сотрудникам Портала
        employees.forEach(hr -> {
            BioPerson personWithHrId = bioPersonMapByOuterId.get(hr.getId().toString());
            if (personWithHrId != null) {
                Long personId = personWithHrId.getId();
                // получаем список positionIds на которых сотрудник сейчас работает
                List<Long> positionIds = hrEmployeePositionMapByEmployeeId.containsKey(hr.getId()) ?
                        hrEmployeePositionMapByEmployeeId.get(hr.getId()).stream().map(EmployeePosition::getPositionId).collect(Collectors.toList())
                        : null;
                boolean isActive = personWithHrId.isActive();
                try {
                    if (positionIds != null && !personWithHrId.isActive()) {
                        personWithHrId.setActive(true);
                        bioPersonRepository.save(personWithHrId);
                    }

                    if (positionIds != null) { // Если человек сейчас работает (есть активные EmployeePositions)
                        // Формируем Map<orgUnitId, Название позиции>
                        Map<Long, String> orgUnitIdsAndPositionNames = new HashMap<>();
                        positionIds.forEach(p -> {
                            Position position = hrPositionMapById.get(p);
                            if (position != null) {
                                if (!orgUnitIdsAndPositionNames.containsKey(p) && (position.getEndDate() == null || currentDate.before(position.getEndDate()) || currentDate.equals(position.getEndDate()))) {
                                    orgUnitIdsAndPositionNames.put(position.getOrgUnitId(), position.getName());
                                }
                            }
                        });
                        // переделываем Map -> формируем Map<personGroupIds, Название позиции>
                        Map<Long, String> personGroupIdsAndPositionNames = new HashMap<>();
                        orgUnitIdsAndPositionNames.forEach((orgUnitId, positionName) -> {
                            Long personGroupId = bioPersonGroupMapByOuterId.get(orgUnitId.toString()).getId();
                            personGroupIdsAndPositionNames.put(personGroupId, positionName);
                        });
                        // Если уже были добавлены связи по позициям ранее
                        if (bioPersonGroupPersonPersonGroupIdsMapByPersonId.containsKey(personId)) {
                            // Смотрим, если состав занятых позиций у сотрудника изменился, то обновляем все связи
                            if (personGroupIdsAndPositionNames.size() != bioPersonGroupPersonPersonGroupIdsMapByPersonId.get(personId).size() ||
                                    !personGroupIdsAndPositionNames.keySet().containsAll(bioPersonGroupPersonPersonGroupIdsMapByPersonId.get(personId))) {

                                QPersonGroupPerson qpgp = QPersonGroupPerson.personGroupPerson;
                                final Iterable<PersonGroupPerson> allDel = personGroupPersonRepository.findAll(qpgp.personId.eq(personId));
                                personGroupPersonRepository.deleteAll(allDel);
                                personGroupIdsAndPositionNames.forEach((id, name) -> {
                                    final PersonGroupPerson pgp = new PersonGroupPerson().setPersonId(personId).setPersonGroupId(id).setPositionName(name);
                                    personGroupPersonRepository.save(pgp);
                                });
                            }
                            // проверка на актуальность названий позиций
                            for (Map.Entry<Long, String> entry : personGroupIdsAndPositionNames.entrySet()) {
                                Long personGroupId = entry.getKey();
                                String actualPositionName = entry.getValue();
                                String oldPositionName = bioPersonGroupPersonPersonIdsAndPositionNamesMapByPersonId.get(personId).get(personGroupId);
                                if (!actualPositionName.equals(oldPositionName)) {
                                    QPersonGroupPerson qpgp = QPersonGroupPerson.personGroupPerson;
                                    final Iterable<PersonGroupPerson> allUpd = personGroupPersonRepository.findAll(qpgp.personId.eq(personId).and(qpgp.personGroupId.eq(personGroupId)));
                                    allUpd.forEach(p -> p.setPositionName(actualPositionName));
                                    personGroupPersonRepository.saveAll(allUpd);
                                }
                            }
                        } else { // Если еще не добавлены связки по позиции
                            personGroupIdsAndPositionNames.forEach((id, name) -> {
                                final PersonGroupPerson pgp = new PersonGroupPerson().setPersonId(personId).setPersonGroupId(id).setPositionName(name);
                                personGroupPersonRepository.save(pgp);
                            });
                        }
                    } else if (isActive || bioPersonGroupPersonPersonGroupIdsMapByPersonId.containsKey(personId)) { // Если сотрудник уволен
                        QPersonGroupPerson pgp = QPersonGroupPerson.personGroupPerson;
                        final Iterable<PersonGroupPerson> allDel = personGroupPersonRepository.findAll(pgp.personId.eq(personId));
                        personGroupPersonRepository.deleteAll(allDel);

                        BioPerson bioPerson = bioPersonRepository.findOneById(personId);
                        bioPerson.setActive(false);
                        if (integrationBioDeleteDescriptors) {
                            List<Long> faceDescriptorIds = getFaceDescriptorIds(bioPerson);
                            if (faceDescriptorIds != null) {
                                deletePhotos(faceDescriptorIds);
                                deleteFaceDescriptors(faceDescriptorIds);
                            }
                        }
                        bioPersonRepository.save(bioPerson);
                    }
                } catch (Exception e) {
                    log.error("ERROR: " + e.toString());
                }
            }
        });
        log.info("Integration of positions WFM->BIO finished!");
    }

    public List<Long> getFaceDescriptorIds(BioPerson person) {
        HttpHeaders headers = createHeaders(links.BIO_LOGIN, links.BIO_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(links.BIO_URL + links.BIO_URL_FACE_DESCRIPTORS)
                .queryParam("person-id", person.getOuterId());

        ResponseEntity<List<BioFaceDescriptorDto>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<BioFaceDescriptorDto>>() {});
        if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody() != null) {
            return response.getBody().stream().map(BioFaceDescriptorDto::getId).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private HttpHeaders createHeaders(String login, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(login, password);
        return headers;
    }

    public ResponseEntity<String> deletePhotos(List<Long> faceDescriptorIds) {
        HttpHeaders headers = createHeaders(links.BIO_LOGIN, links.BIO_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(links.BIO_URL +
                        links.BIO_URL_FACE_DESCRIPTORS +
                        links.BIO_DELETE_PHOTOS)
                .queryParam("ids", faceDescriptorIds.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining(",")));

        return restTemplate.exchange(builder.toUriString(),
                HttpMethod.POST,
                entity,
                String.class);
    }

    public ResponseEntity<String> deleteFaceDescriptors(List<Long> faceDescriptorIds) {
        HttpHeaders headers = createHeaders(links.BIO_LOGIN, links.BIO_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(links.BIO_URL +
                        links.BIO_URL_FACE_DESCRIPTORS +
                        links.BIO_DELETE_FACE_DESCRIPTORS)
                .queryParam("ids", faceDescriptorIds.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining(",")));

        return restTemplate.exchange(builder.toUriString(),
                HttpMethod.POST,
                entity,
                String.class);
    }

}
