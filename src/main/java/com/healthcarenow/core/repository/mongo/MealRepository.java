package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.Meal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MealRepository extends MongoRepository<Meal, String> {
  List<Meal> findByUserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);
}
