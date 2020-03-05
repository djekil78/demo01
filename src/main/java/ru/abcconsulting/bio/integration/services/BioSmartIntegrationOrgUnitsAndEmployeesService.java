package ru.abcconsulting.bio.integration.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.abcconsulting.bio.integration.dto.*;
import ru.abcconsulting.bio.integration.entity.wfm.Employee;
import ru.abcconsulting.bio.integration.entity.wfm.EmployeePosition;
import ru.abcconsulting.bio.integration.entity.wfm.OrgUnit;
import ru.abcconsulting.bio.integration.entity.wfm.Position;
import ru.abcconsulting.bio.integration.repository.wfm.EmployeePositionRepository;
import ru.abcconsulting.bio.integration.repository.wfm.EmployeeRepository;
import ru.abcconsulting.bio.integration.repository.wfm.OrgUnitRepository;
import ru.abcconsulting.bio.integration.repository.wfm.PositionRepository;
import ru.abcconsulting.bio.integration.util.Links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class BioSmartIntegrationOrgUnitsAndEmployeesService {

    private final RestTemplate restTemplate;
    private final Links links;
    private final OrgUnitRepository orgUnitRepository;
    private final PositionRepository positionRepository;
    private final EmployeePositionRepository employeePositionRepository;
    private final EmployeeRepository employeeRepository;

    public void testConnectAndGetData() {
        String token = getToken();

        List<BioSmartOrgUnitDto> bioSmartOrgUnitDtos = getAllOrgUnits(token);
        System.out.println(bioSmartOrgUnitDtos);

/*        Long createdId = createOrgUnit(token, "╨Я╤А╨╛╨▒╨╜╨░╤П ╨│╨╗╨░╨▓╨╜╨░╤П ╤Д╨╕╤А╨╝╨░", "123", null, true);
        System.out.println("OrgUnit created id =  " + createdId); */
        updateOrgUnit(token, 67235636L, "╨Я╤А╨╛╨▒╨╜╨░╤П ╤Д╨╕╤А╨╝╤Г", null, null, 67235636L);
    }

    @Scheduled(cron = "${integration.biosmart.orgstructure.cron}")
    public void integrationByScheduler() throws Exception {
        final List<OrgUnit> orgUnits = orgUnitRepository.findAll();
        integrateOrgUnits(orgUnits);

        final List<Position> positions = positionRepository.findAll();
        final List<EmployeePosition> employeePositions = employeePositionRepository.findAll();
        final List<Employee> employees = employeeRepository.findAll();
        integrateEmployees(positions, employeePositions, employees);
    }


    public void integrateOrgUnits(List<OrgUnit> orgUnits) throws Exception {

        log.info("Integration of orgunits WFM->BIOSmart starts!");
        List<Long> orgUnitIds = links.getOrgUnitIds();

        String token = getToken();
        Map<String, BioSmartOrgUnitDto> bioSmartOrgUnitsMapByDepnum = new HashMap<>();

        String firmOrgUnitName = links.getMainOrgUnitName();
        Long firmOrgUnitId = null; // id ╨│╨╗╨░╨▓╨╜╨╛╨│╨╛ ╨╛╤А╨│╤О╨╜╨╕╤В╨░ ╨▓ ╨С╨╕╨╛╨б╨╝╨░╤А╤В
        List<BioSmartOrgUnitDto> bioSmartOrgUnitDtos = getAllOrgUnits(token);
        for (BioSmartOrgUnitDto v : bioSmartOrgUnitDtos) {
            bioSmartOrgUnitsMapByDepnum.put(v.getDepnum(), v);
            if (v.getName().equals(firmOrgUnitName)) {
                firmOrgUnitId = v.getId();
            }
        }

        // ╨Ш╨╜╤В╨╡╨│╤А╨╕╤А╤Г╨╡╨╝ ╤В╨╛╨╗╤М╨║╨╛ ╤А╨░╨╖╤А╨╡╤И╨╡╨╜╨╜╤Л╨╡ ╨╛╤А╨│╤О╨╜╨╕╤В╤Л ╨╕ ╨╛╤Б╨╜╨╛╨▓╨╜╨╛╨╣ ╤А╨╛╨┤╨╕╤В╨╡╨╗╤М╤Б╨║╨╕╨╣
        List<OrgUnit> hrPortalOrgUnits = orgUnits.stream().filter(e -> orgUnitIds.contains(e.getId())).collect(toList());

        // ╨┐╤А╨╛╨▓╨╡╤А╨║╨░ ╨╜╨░ ╨╜╨░╨╗╨╕╤З╨╕╨╡ ╨╛╤Б╨╜╨╛╨▓╨╜╨╛╨│╨╛ ╨╛╤А╨│╤О╨╜╨╕╤В╨░ "╨┐╤А╨╡╨┤╨┐╤А╨╕╤П╤В╨╕╤П"
/*        OrgUnitDto mainOrgUnit = null;
        Long mainOrgUnitId = null; // id ╨│╨╗╨░╨▓╨╜╨╛╨│╨╛ ╨╛╤А╨│╤О╨╜╨╕╤В╨░ ╨▓ ╨Я╨╛╤А╤В╨░╨╗╨╡
        for (OrgUnitDto o : hrPortalOrgUnits) {
            if (o.getParentId() == null || o.getParentId() == 0) {
                mainOrgUnit = o;
            }
        } */

        if (firmOrgUnitId == null) {
            firmOrgUnitId = createOrgUnit(token, firmOrgUnitName, null, null, null, true); // ╤Б╨╛╨╖╨┤╨░╨╡╨╝ ╨│╨╗╨░╨▓╨╜╤Л╨╣ ╨╛╤А╨│╤О╨╜╨╕╤В
        }

        if (firmOrgUnitId == null) {
            throw new Exception("Main orgUnit not found");
        }

        // ╨Я╨╡╤А╨╡╨╜╨╛╤Б╨╕╨╝ ╨╛╤А╨│╤О╨╜╨╕╤В╤Л ╨▓ ╨▒╨╕╨╛╤Б╨╝╨░╤А╤В (╨║╤А╨╛╨╝╨╡ ╤Г╨╢╨╡ ╤Б╤Г╤Й╨╡╤Б╤В╨▓╤Г╤О╤Й╨╕╤Е)
        for (OrgUnit hr : hrPortalOrgUnits) {
            if (!bioSmartOrgUnitsMapByDepnum.containsKey(hr.getId().toString())) {
                createOrgUnit(token, hr.getName(), hr.getId().toString(), firmOrgUnitId, null, false);
            }
        }

        // ╨Ф╨╛╨▒╨░╨▓╨╗╤П╨╡╨╝ ╨╕╨╜╤Д╨╛╤А╨╝╨░╤Ж╨╕╤О ╨▓ ╨┐╨╛╨╗╨╡ parent
/*        Map<String, BioSmartOrgUnitDto> bioSmartOrgUnitsMapByDepnumAfterIntegration = new HashMap<>();
        getAllOrgUnits(token).forEach(v -> bioSmartOrgUnitsMapByDepnumAfterIntegration.put(v.getDepnum(), v));

        for (OrgUnitDto hr : hrPortalOrgUnits) {
            if (hr.getParentId() != null) {
                BioSmartOrgUnitDto bioSmartOrgUnitDto = bioSmartOrgUnitsMapByDepnumAfterIntegration.get(hr.getId().toString());
                Long bioSmartParentId = bioSmartOrgUnitsMapByDepnumAfterIntegration.get(hr.getParentId().toString()) != null ?
                    bioSmartOrgUnitsMapByDepnumAfterIntegration.get(hr.getParentId().toString()).getId() : null;
                if (bioSmartParentId != null && !bioSmartParentId.equals(bioSmartOrgUnitDto.getParent())) {
                    updateOrgUnit(token, bioSmartOrgUnitDto.getId(), bioSmartOrgUnitDto.getName(),
                            bioSmartOrgUnitDto.getDepnum(), bioSmartParentId, bioSmartOrgUnitDto.getFirm());
                }
            }
        } */
        log.info("Integration of orgunits WFM->BIOSmart finished!");
    }

    public void integrateEmployees(List<Position> positions,
                                   List<EmployeePosition> employeePositions,
                                   List<Employee> employees) {
        log.info("Integration of employees WFM->BIOSmart starts!");
        List<Long> orgUnitIds = links.getOrgUnitIds();

        String token = getToken();
        Map<String, BioSmartEmployeeDto> bioSmartEmployeesByWorkernum = new HashMap<>();
        getAllEmployees(token).forEach(v -> bioSmartEmployeesByWorkernum.put(v.getWorkernum(), v));

        Map<String, BioSmartOrgUnitDto> bioSmartOrgUnitsMapByDepnum = new HashMap<>();
        List<BioSmartOrgUnitDto> bioSmartOrgUnitDtos = getAllOrgUnits(token);
        bioSmartOrgUnitDtos.forEach(v -> bioSmartOrgUnitsMapByDepnum.put(v.getDepnum(), v));


        /*Map<Long, Employee> hrEmployeeMapById = new HashMap<>();
        //╨┤╨╛╨▒╨░╨▓╨╗╤П╨╡╨╝ ╤В╨╛╨╗╤М╨║╨╛ ╤В╨╡ ╨╖╨░╨┐╨╕╤Б╨╕, ╨│╨┤╨╡ endDate = null
        integrationService.refreshAndGetHrEmployees().forEach(v -> {
            if (v.getEndWorkDate() == null) {
                hrEmployeeMapById.put(v.getId(), v);
            }
        });*/

        Map<Long, Position> hrPositionMapByPositionId = new HashMap<>();
        positions.forEach(v -> {
            if (v.getEndDate() == null) {
                hrPositionMapByPositionId.put(v.getId(), v);
            }
        });

        Map<Long, ComposedEmployeePositionDto> hrComposedEmployeePositionMapByEmployeeId = new HashMap<>();
        employeePositions.forEach(v -> {
            Long positionId = v.getPositionId();
            if (hrPositionMapByPositionId.containsKey(positionId) && v.getEndDate() == null) {
                Position positionDto = hrPositionMapByPositionId.get((positionId));
                if (orgUnitIds.contains(positionDto.getOrgUnitId())) {
                    ComposedEmployeePositionDto composedEmployeePositionDto = new ComposedEmployeePositionDto();
                    composedEmployeePositionDto.setEmployeeId(v.getEmployeeId());
                    composedEmployeePositionDto.setPositionId(positionDto.getId());
                    composedEmployeePositionDto.setPositionName(positionDto.getName());
                    composedEmployeePositionDto.setOrgUnitId(positionDto.getOrgUnitId());
                    hrComposedEmployeePositionMapByEmployeeId.put(v.getEmployeeId(), composedEmployeePositionDto);
                }
            }
        });

        for (Employee hr : employees) {
            if (!bioSmartEmployeesByWorkernum.containsKey(hr.getId().toString()) && hrComposedEmployeePositionMapByEmployeeId.containsKey(hr.getId())) {
                ComposedEmployeePositionDto composedEmployeePositionDto = hrComposedEmployeePositionMapByEmployeeId.get(hr.getId());
                if (composedEmployeePositionDto != null) {
                    String orgUnitId = composedEmployeePositionDto.getOrgUnitId().toString();
                    Long bioSmartOrgUnitId = null;
                    if (bioSmartOrgUnitsMapByDepnum.containsKey(orgUnitId)) {
                        bioSmartOrgUnitId = bioSmartOrgUnitsMapByDepnum.get(orgUnitId).getId();
                    }
                    Long bioSmartJobId = createJob(token, composedEmployeePositionDto.getPositionName());
                    createEmployee(token, hr.getFirstName(), hr.getLastName(), hr.getPatronymicName(), hr.getId().toString(), bioSmartOrgUnitId, bioSmartJobId);
                }
            }
        }
        log.info("Integration of employees WFM->BIOSmart finished!");
    }


    private String getToken() {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setBasicAuth(links.LOGIN, links.PASSWORD);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        final ResponseEntity<BioSmartAuthDto> response = restTemplate.postForEntity(links.URL_BIO_SMART + links.URL_LOGIN, request, BioSmartAuthDto.class);
        return response.getBody().getToken();
    }

    public List<BioSmartEmployeeDto> getAllEmployees(String token) {
        // GET Employees
        HttpEntity entity = createHttpEntity(token);
        ResponseEntity<List<BioSmartEmployeeDto>> responseEmpl = restTemplate.exchange(
                links.URL_BIO_SMART + links.URL_EMPLOYEES, HttpMethod.GET, entity, new ParameterizedTypeReference<List<BioSmartEmployeeDto>>() {
                });
        return responseEmpl.getBody();
    }

    public List<BioSmartOrgUnitDto> getAllOrgUnits(String token) {
        // GET OrgUnits
        HttpEntity entity = createHttpEntity(token);
        ResponseEntity<List<BioSmartOrgUnitDto>> responseOrg = restTemplate.exchange(
                links.URL_BIO_SMART + links.URL_ORGSTRUCTURE, HttpMethod.GET, entity, new ParameterizedTypeReference<List<BioSmartOrgUnitDto>>() {
                });
        return responseOrg.getBody();

    }

    private Long createOrgUnit(String token, String name, String outerId, Long firm, Long parent, Boolean isFirm) {
        // Create OrgUnit
        HttpHeaders headersOrg = setHeaders(token);

/*        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        HttpEntity<BioSmartOrgUnitDto> requestToCreate = new HttpEntity<>(orgUnitDto, headersOrg);
        restTemplate.postForObject(URL_ORGSTRUCTURE_CREATE, requestToCreate, String.class); */

        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("depnum", outerId);
        body.add("name", name);
        if (firm != null) {
            body.add("firm", firm.toString());
        }
        if (isFirm != null) {
            body.add("is_firm", isFirm.toString());
        }
        if (parent != null) {
            body.add("parent", parent.toString());
        } else if (firm != null) {
            body.add("parent", firm.toString());
        }
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, headersOrg);
        ResponseEntity<BioSmartOrgUnitDto> result = restTemplate.exchange(links.URL_BIO_SMART + links.URL_ORGSTRUCTURE, HttpMethod.POST, httpEntity, BioSmartOrgUnitDto.class);
        return result.getBody().getId();
    }

    private Long updateOrgUnit(String token, Long id, String name, String outerId, Long parent, Long firm) {
        // Create OrgUnit
        HttpHeaders headersOrg = new HttpHeaders();
        headersOrg.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headersOrg.set("Authorization", "Token " + token);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("depnum", outerId);
        body.add("name", name);
        if (parent != null) {
            body.add("parent", parent.toString());
        } else {
            body.add("parent", null);
        }
        body.add("firm", firm.toString());
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, headersOrg);
        ResponseEntity<BioSmartOrgUnitDto> result = restTemplate.exchange(links.URL_BIO_SMART + links.URL_ORGSTRUCTURE + "/" + id, HttpMethod.PUT, httpEntity, BioSmartOrgUnitDto.class);
        return result.getBody().getId();
    }

    private Long createEmployee(String token, String firstName, String lastName, String middleName, String workernum, Long bioSmartOrgUnitId, Long bioSmartJobId) {
        // Create Employee
        HttpHeaders headersOrg = setHeaders(token);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("first_name", firstName);
        body.add("last_name", lastName);
        body.add("middle_name", middleName);
        body.add("workernum", workernum);
        if (bioSmartOrgUnitId != null) {
            body.add("parent", bioSmartOrgUnitId.toString());
        }
        if (bioSmartJobId != null) {
            body.add("job", bioSmartJobId.toString());
        }


        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, headersOrg);
        ResponseEntity<BioSmartEmployeeDto> result = restTemplate.exchange(links.URL_BIO_SMART + links.URL_EMPLOYEES, HttpMethod.POST, httpEntity, BioSmartEmployeeDto.class);
        return result.getBody().getId();
    }

    private Long createJob(String token, String jobName) {
        // Create Job
        HttpHeaders headersOrg = setHeaders(token);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("name", jobName);

        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, headersOrg);
        ResponseEntity<BioSmartJobDto> result = restTemplate.exchange(links.URL_BIO_SMART + links.URL_JOB, HttpMethod.POST, httpEntity, BioSmartJobDto.class);
        return result.getBody().getId();
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
}
