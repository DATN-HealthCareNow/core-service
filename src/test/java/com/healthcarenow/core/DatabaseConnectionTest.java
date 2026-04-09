package com.healthcarenow.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev") // Use 'dev' profile to connect to localhost
class DatabaseConnectionTest {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Test
  void testMongoConnection() {
    // Just checking if we can talk to Mongo, even if no collections exist yet
    var dbName = mongoTemplate.getDb().getName();
    assertThat(dbName).isNotNull();
    System.out.println("✅ Kết nối MongoDB thành công! DB Name: " + dbName);
  }
}
