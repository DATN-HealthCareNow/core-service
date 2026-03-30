package com.healthcarenow.core.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcarenow.core.service.WaterIntakeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WaterEventListener { 

    private final ObjectMapper objectMapper;
    private final WaterIntakeService waterIntakeService;

    @RabbitListener(queues = "water.logging.queue")
    public void receiveWaterLoggedEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String userId = node.has("userId") ? node.get("userId").asText() : null;
            Integer amountMl = node.has("amountMl") ? node.get("amountMl").asInt() : null;
            String dateString = node.has("dateString") ? node.get("dateString").asText() : null;

            if (userId != null && amountMl != null && dateString != null) {
                log.info("Received water log event for userId: {}, amount: {} ml", userId, amountMl);
                waterIntakeService.handleWaterLoggedEvent(userId, amountMl, dateString);
            } else {
                log.warn("Invalid water log event message: {}", message);
            }
        } catch (Exception e) {
            log.error("Error processing water log event: {}", e.getMessage(), e);
        }
    }
}