package com.healthcarenow.core.scheduler;

import com.healthcarenow.core.config.RabbitMQConfig;
import com.healthcarenow.core.dto.NotificationEvent;
import com.healthcarenow.core.model.jpa.User;
import com.healthcarenow.core.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WaterReminderScheduler {

  private final UserRepository userRepository;
  private final RabbitTemplate rabbitTemplate;

  // Run at 07:00, 12:00, and 18:00 every day in Vietnam timezone
  @Scheduled(
      cron = "0 0 7,12,18 * * ?",
      zone = "Asia/Ho_Chi_Minh"
  )
  public void triggerWaterReminders() {
    log.info("[WATER_REMINDER] Starting to send water reminders to active users...");
    List<User> activeUsers = userRepository.findByStatus("ACTIVE");

    for (User user : activeUsers) {
      try {
        Map<String, String> payload = new HashMap<>();
        payload.put("title", "Đã đến giờ uống nước!");
        payload.put("body", "Hãy uống một cốc nước để duy trì sức khỏe nhé!");
        payload.put("language", "vi");

        NotificationEvent event = NotificationEvent.builder()
            .eventType("WATER_REMIND")
            .userId(user.getId())
            .priority("NORMAL")
            .payload(payload)
            .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, event);
        log.debug("Sent WATER_REMIND event for user: {}", user.getId());
      } catch (Exception e) {
        log.error("Failed to send water reminder for user {}: {}", user.getId(), e.getMessage());
      }
    }
    
    log.info("[WATER_REMINDER] Finished sending water reminders. Count={}", activeUsers.size());
  }
}
