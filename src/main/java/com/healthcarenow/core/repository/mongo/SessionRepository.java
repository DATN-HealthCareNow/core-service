package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {
  Optional<Session> findByTokenHash(String tokenHash);
}
