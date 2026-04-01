package com.healthcarenow.core.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcarenow.core.service.WaterIntakeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class WaterEventListener { 

    private final ObjectMapper objectMapper;
    private final WaterIntakeService waterIntakeService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "event:water:processed:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofDays(2);

    @RabbitListener(queues = "water.logging.queue")
    public void receiveWaterLoggedEvent(org.springframework.amqp.core.Message messageObj) {
        String eventId = null;
        try {
            String message = new String(messageObj.getBody());
            JsonNode node = objectMapper.readTree(message);

            JsonNode bodyNode = node.has("payload") ? node.get("payload") : node;
            String userId = getText(bodyNode, "user_id", "userId");
            Integer amountMl = getInt(bodyNode, "amount_ml", "amountMl");
            String dateString = getText(bodyNode, "date_string", "dateString");

            String eventType = getText(node, "event_type", "eventType");
            Integer eventVersion = getInt(node, "event_version", "eventVersion");
            String correlationId = getText(node, "correlation_id", "correlationId");
            eventId = getText(node, "event_id", "eventId");

            if (eventId != null) {
                Boolean firstTime = redisTemplate.opsForValue()
                    .setIfAbsent(IDEMPOTENCY_KEY_PREFIX + eventId, "1", IDEMPOTENCY_TTL);

                if (Boolean.FALSE.equals(firstTime)) {
                    log.warn("Skip duplicated water event eventId={}", eventId);
                    return;
                }
            }

            if (userId != null && amountMl != null && dateString != null) {
                if (eventType != null || eventVersion != null || correlationId != null) {
                    log.info("Received water event type={}, version={}, correlationId={}", eventType, eventVersion, correlationId);
                }
                log.info("Received water log event for userId: {}, amount: {} ml", userId, amountMl);
                waterIntakeService.handleWaterLoggedEvent(userId, amountMl, dateString);
            } else {
                log.warn("Invalid water log event message: {}", message);
                if (eventId != null) {
                    redisTemplate.delete(IDEMPOTENCY_KEY_PREFIX + eventId);
                }
                throw new AmqpRejectAndDontRequeueException("Invalid water event payload");
            }
        } catch (Exception e) {
            if (eventId != null) {
                redisTemplate.delete(IDEMPOTENCY_KEY_PREFIX + eventId);
            }
            log.error("Error processing water log event: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Failed to process water event", e);
        }
    }

    private String getText(JsonNode node, String snakeCase, String camelCase) {
        if (node == null) {
            return null;
        }
        if (node.hasNonNull(snakeCase)) {
            return node.get(snakeCase).asText();
        }
        if (node.hasNonNull(camelCase)) {
            return node.get(camelCase).asText();
        }
        return null;
    }

    private Integer getInt(JsonNode node, String snakeCase, String camelCase) {
        if (node == null) {
            return null;
        }
        if (node.hasNonNull(snakeCase)) {
            return node.get(snakeCase).asInt();
        }
        if (node.hasNonNull(camelCase)) {
            return node.get(camelCase).asInt();
        }
        return null;
    }
}