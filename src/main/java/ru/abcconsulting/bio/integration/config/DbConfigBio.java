package ru.abcconsulting.bio.integration.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
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
        entityManagerFactoryRef = "bioEntityManagerFactory",
        transactionManagerRef = "bioTransactionManager",
        basePackages = {"ru.abcconsulting.bio.integration.repository.bio"}
)
public class DbConfigBio extends BaseDbConfig {

    @Bean(name = "dataSourceBio")
    public DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(integrationBioDbDriver);
        dataSource.setUrl(integrationBioDbUrl);
        dataSource.setUsername(integrationBioDbUsername);
        dataSource.setPassword(integrationBioDbPassword);
        return dataSource;
    }

    @Bean(name = "bioEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean createEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(createDataSource());
        em.setPackagesToScan(new String[] { "ru.abcconsulting.bio.integration.entity.bio" });
        em.setPersistenceUnitName("bio");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());
        return em;
    }

    @Bean(name = "bioTransactionManager")
    public PlatformTransactionManager createTransactionManager(@Qualifier("bioEntityManagerFactory") EntityManagerFactory emf) {
        final JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(emf);
        return tm;
    }

    @Bean(name = "bioJdbcTemplate")
    public JdbcTemplate createJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(createDataSource());
        return jdbcTemplate;
    }

    @Override
    public Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.ddl-auto", "validate");
        properties.setProperty("hibernate.naming-strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
        properties.setProperty("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation", "true");
        properties.setProperty("spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.setProperty(AvailableSettings.DIALECT, integrationBioDbDialect);
        return properties;
    }

}
