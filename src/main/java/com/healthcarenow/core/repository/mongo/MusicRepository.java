package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.MusicFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MusicRepository extends MongoRepository<MusicFile, String> {
    
    List<MusicFile> findByUserId(String userId);
    
    List<MusicFile> findByUserIdAndStatus(String userId, String status);
    
    Optional<MusicFile> findByIdAndUserId(String id, String userId);
    
    void deleteByIdAndUserId(String id, String userId);
    
    List<MusicFile> findByIsDefaultTrue();
}
