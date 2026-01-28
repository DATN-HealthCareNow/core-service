package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.SleepSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SleepSessionRepository extends MongoRepository<SleepSession, String> {
  List<SleepSession> findByUserId(String userId);
}
