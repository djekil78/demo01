package ru.abcconsulting.bio.integration.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.abcconsulting.bio.integration.services.BioSmartIntegrationOrgUnitsAndEmployeesService;
import ru.abcconsulting.bio.integration.services.BioSmartIntegrationRecordsService;

@Slf4j
@RestController
@RequestMapping(value = "/bio-smart")
public class BioSmartController {

    @Autowired
    private BioSmartIntegrationRecordsService bioSmartIntegrationRecordsService;

    @Autowired
    private BioSmartIntegrationOrgUnitsAndEmployeesService bioSmartIntegrationOrgUnitsAndEmployeesService;

    @RequestMapping(method = RequestMethod.GET, value = "/orgstructure")
    public ResponseEntity<HttpStatus> integrateOrgstructure() throws Exception {
        log.info("BioSmartController GET /bio-smart/orgstructure: method is called");
        bioSmartIntegrationOrgUnitsAndEmployeesService.integrationByScheduler();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/records")
    public ResponseEntity<HttpStatus> integrateRecords() {
        log.info("BioSmartController GET /bio-smart/records: method is called");
        bioSmartIntegrationRecordsService.integrationRecords();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
