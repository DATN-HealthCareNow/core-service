package com.healthcarenow.core.scheduler;

import com.healthcarenow.core.model.mongo.MedicalRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordScheduler {

    private final MongoTemplate mongoTemplate;

    /**
     * Chạy mỗi giờ để kiểm tra và đóng các đơn thuốc đã hết hạn.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoExpireRecords() {
        LocalDateTime now = LocalDateTime.now();
        
        Query query = new Query(
            Criteria.where("status").is("ACTIVE")
                .and("expiryDate").lt(now)
        );

        Update update = new Update()
            .set("status", "EXPIRED")
            .set("updatedAt", now);

        long modifiedCount = mongoTemplate.updateMulti(query, update, MedicalRecord.class).getModifiedCount();
        
        if (modifiedCount > 0) {
            log.info("[MedicalRecordScheduler] Auto-expired {} medical records at {}", modifiedCount, now);
        }
    }
}
