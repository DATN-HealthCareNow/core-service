package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
  List<MedicalRecord> findByUserId(String userId);
}
