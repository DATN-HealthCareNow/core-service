package com.healthcarenow.core.listener;

import com.healthcarenow.core.config.RabbitMQConfig;
import com.healthcarenow.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationCleanupListener {

    private final UserService userService;

    @RabbitListener(queues = RabbitMQConfig.MEDICATION_CLEANUP_QUEUE)
    public void handleMedicationCleanup(Map<String, Object> message) {
        log.info("[MedicationCleanupListener] Received cleanup event: {}", message);
        
        try {
            String userId = (String) message.get("userId");
            String sourceId = (String) message.get("sourceId");
            
            if (userId != null && sourceId != null) {
                userService.removeForbiddenFoodsBySource(userId, sourceId);
                log.info("[MedicationCleanupListener] Successfully cleared foods for user {} from source {}", userId, sourceId);
            } else {
                log.warn("[MedicationCleanupListener] Missing userId or sourceId in message: {}", message);
            }
        } catch (Exception e) {
            log.error("[MedicationCleanupListener] Error processing cleanup event", e);
        }
    }
}
