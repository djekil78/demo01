package ru.abcconsulting.bio.integration.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "integrationEntityManagerFactory",
        transactionManagerRef = "integrationTransactionManager",
        basePackages = {"ru.abcconsulting.bio.integration.repository.integration"}
)
public class DbConfigIntegration extends BaseDbConfig {

    @Bean(name = "bioIntegrationDataSource")
    public DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(integrationBioIntegrationDbDriver);
        dataSource.setUrl(integrationBioIntegrationDbUrl);
        dataSource.setUsername(integrationBioIntegrationDbUsername);
        dataSource.setPassword(integrationBioIntegrationDbPassword);
        return dataSource;
    }

    @Bean(name = "integrationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean createEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(createDataSource());
        em.setPackagesToScan(new String[] { "ru.abcconsulting.bio.integration.entity.integration" });
        em.setPersistenceUnitName("integration");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());
        return em;

    }

    @Bean(name = "integrationTransactionManager")
    public PlatformTransactionManager createTransactionManager(@Qualifier("integrationEntityManagerFactory") EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Override
    public Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.ddl-auto", "validate");
        properties.setProperty("hibernate.naming-strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
        properties.setProperty("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation", "true");
        properties.setProperty("spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.setProperty(AvailableSettings.DIALECT, integrationBioIntegrationDbDialect);
        return properties;
    }
}
