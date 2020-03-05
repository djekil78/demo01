package ru.abcconsulting.bio.integration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

public abstract class BaseDbConfig {

    @Value("${integration.hrportal.db.driver}")
    protected String integrationHrPortalDbDriver;

    @Value("${integration.hrportal.db.url}")
    protected String integrationHrPortaDblUrl;

    @Value("${integration.hrportal.db.username}")
    protected String integrationHrPortalDbUsername;

    @Value("${integration.hrportal.db.password}")
    protected String integrationHrPortalDbPassword;

    @Value("${integration.hrportal.db.dialect}")
    protected String integrationHrPortalDbDialect;


    @Value("${integration.bio.db.driver}")
    protected String integrationBioDbDriver;

    @Value("${integration.bio.db.url}")
    protected String integrationBioDbUrl;

    @Value("${integration.bio.db.username}")
    protected String integrationBioDbUsername;

    @Value("${integration.bio.db.password}")
    protected String integrationBioDbPassword;

    @Value("${integration.bio.db.dialect}")
    protected String integrationBioDbDialect;


    @Value("${integration.bio_integration.db.driver}")
    protected String integrationBioIntegrationDbDriver;

    @Value("${integration.bio_integration.db.url}")
    protected String integrationBioIntegrationDbUrl;

    @Value("${integration.bio_integration.db.username}")
    protected String integrationBioIntegrationDbUsername;

    @Value("${integration.bio_integration.db.password}")
    protected String integrationBioIntegrationDbPassword;

    @Value("${integration.bio_integration.db.dialect}")
    protected String integrationBioIntegrationDbDialect;

    public abstract DataSource createDataSource();

    public abstract LocalContainerEntityManagerFactoryBean createEntityManagerFactory();

    public abstract PlatformTransactionManager createTransactionManager(EntityManagerFactory emf);

    public abstract Properties additionalProperties();

}
