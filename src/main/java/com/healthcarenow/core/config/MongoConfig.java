package com.healthcarenow.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "com.healthcarenow.core.repository.mongo"
)
public class MongoConfig {
    // This class serves to isolate the MongoDB repository scanning
    // to prevent it from trying to instantiate JPA repositories.
}
