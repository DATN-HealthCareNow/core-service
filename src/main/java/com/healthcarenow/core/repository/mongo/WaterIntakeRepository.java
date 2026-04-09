package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.WaterIntake;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaterIntakeRepository extends MongoRepository<WaterIntake, String> {
  List<WaterIntake> findByUserIdAndDateString(String userId, String dateString);

  WaterIntake findFirstByUserIdAndDateStringOrderByTimestampDesc(String userId, String dateString);
}
