package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.PatientProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientProfileRepository extends MongoRepository<PatientProfile, String> {
  Optional<PatientProfile> findByUserId(String userId);
}
