package com.healthcarenow.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.healthcarenow.core.repository.jpa", entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
public class JpaConfig {
    // Spring Boot's auto-configuration will handle the entityManagerFactory
    // and transactionManager creation based on the 'spring.datasource'
    // properties in application.yml.
    // This class primarily serves to isolate the JPA repository scanning.
}
