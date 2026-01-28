package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.FoodDatabase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodDatabaseRepository extends MongoRepository<FoodDatabase, String> {
  List<FoodDatabase> findByNameContainingIgnoreCase(String name);
}
