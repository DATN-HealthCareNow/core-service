package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
  List<Prescription> findByUserId(String userId);
}
