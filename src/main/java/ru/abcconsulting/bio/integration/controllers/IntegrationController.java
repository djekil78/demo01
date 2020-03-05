package ru.abcconsulting.bio.integration.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.abcconsulting.bio.integration.services.GenerateTestDataService;
import ru.abcconsulting.bio.integration.services.IntegrationRecordsService;
import ru.abcconsulting.bio.integration.services.IntegrationService;

@Slf4j
@RestController
@RequestMapping(value = "/integration")
public class IntegrationController {

    @Autowired
    private IntegrationService integrationService;

    @Autowired
    private IntegrationRecordsService integrationRecordsService;

    @Autowired
    private GenerateTestDataService GenerateTestDataService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<HttpStatus> integrate() {
        log.info("IntegrationController GET /integration: method is called");
        integrationService.integrationByScheduler();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/record", method = RequestMethod.GET)
    public ResponseEntity<HttpStatus> integrate1() {
        log.info("IntegrationController GET /integration/record: method is called");
        integrationRecordsService.integrationByScheduler();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/testdata", method = RequestMethod.GET)
    public ResponseEntity<HttpStatus> integrate2() {
        log.info("IntegrationController GET /integration/testdata: method is called");
        GenerateTestDataService.generateTestData();
        return ResponseEntity.ok().build();
    }
}
