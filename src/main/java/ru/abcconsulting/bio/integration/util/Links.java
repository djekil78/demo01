package ru.abcconsulting.bio.integration.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PropertySource("file:${config.dir}/integration.properties")
public class Links {
    @Value("${integration.biosmart.url}")
    public String URL_BIO_SMART;

    @Value("${integration.biosmart.login}")
    public String LOGIN;

    @Value("${integration.biosmart.password}")
    public String PASSWORD;

    @Value("#{'${integration.biosmart.permitted.orgunitids}'.split(',')}")
    private List<Long> orgUnitIds;

    @Value("${integration.biosmart.mainOrgunit.name}")
    private String mainOrgUnitName;

    @Value("${integration.biosmart.logs.timezone.default}")
    public String defaultTimeZone;

    public String URL_LOGIN                     = "/auth/login";
    public String URL_ORGSTRUCTURE              = "/orgstructure";
    public String URL_LOGS                      = "/logs";
    public String URL_EMPLOYEES                 = "/employee";
    public String URL_JOB                       = "/job";
    public String LOGS_MAX_PAGE_SIZE            = "100";

    public List<Long> getOrgUnitIds() {
        return orgUnitIds;
    }

    public String getMainOrgUnitName() {
        return mainOrgUnitName;
    }

    @Value("${integration.bio.api.url:}")
    public String BIO_URL;

    @Value("${integration.bio.api.login:}")
    public String BIO_LOGIN;

    @Value("${integration.bio.api.password:}")
    public String BIO_PASSWORD;

    public String BIO_URL_FACE_DESCRIPTORS = "/face-descriptors";
    public String BIO_DELETE_FACE_DESCRIPTORS = "/delete";
    public String BIO_DELETE_PHOTOS = "/delete-photos";
}
