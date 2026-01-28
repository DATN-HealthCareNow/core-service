package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.HealthScore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthScoreRepository extends MongoRepository<HealthScore, String> {
  Optional<HealthScore> findTopByUserIdOrderByDateDesc(String userId);
}
