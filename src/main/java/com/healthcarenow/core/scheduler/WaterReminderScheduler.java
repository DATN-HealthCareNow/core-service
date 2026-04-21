package com.healthcarenow.core.scheduler;

import com.healthcarenow.core.config.RabbitMQConfig;
import com.healthcarenow.core.dto.NotificationEvent;
import com.healthcarenow.core.model.mongo.User;
import com.healthcarenow.core.repository.mongo.UserRepository;
import com.healthcarenow.core.service.WaterIntakeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "notification.scheduler.core", name = "enabled", havingValue = "true")
public class WaterReminderScheduler {

  private final UserRepository userRepository;
  private final RabbitTemplate rabbitTemplate;

  private final WaterIntakeService waterIntakeService;

  @Scheduled(cron = "0 0 7,12,18 * * ?", zone = "Asia/Ho_Chi_Minh")
  public void triggerWaterReminders() {
    log.info("[WATER_REMINDER] Starting to send water reminders to active users...");
    List<User> activeUsers = userRepository.findByStatus("ACTIVE");

    for (User user : activeUsers) {
      try {
        var progress = waterIntakeService.getTodayWaterIntake(user.getId());
        int currentMl = progress.getTotalTodayMl() != null ? progress.getTotalTodayMl() : 0;
        int goalMl = progress.getGoalMl() != null ? progress.getGoalMl() : 2000;
        int neededMl = Math.max(goalMl - currentMl, 0);

        if (neededMl <= 0) {
          log.debug("User {} already met water goal, skipping reminder", user.getId());
          continue;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Đã đến giờ uống nước!");
        payload.put("body", String.format("Bạn cần uống thêm %d ml nước nữa để đạt mục tiêu ngày hôm nay (%d/%d ml). Hãy uống ngay nhé!", 
            neededMl, currentMl, goalMl));
        payload.put("language", "vi");
        payload.put("neededMl", neededMl);
        payload.put("currentMl", currentMl);
        payload.put("goalMl", goalMl);

        NotificationEvent event = NotificationEvent.builder()
            .eventType("WATER_REMINDER")
            .userId(user.getId())
            .priority("NORMAL")
            .payload(payload)
            .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, event);
        log.debug("Sent WATER_REMINDER event for user: {} (needed: {}ml)", user.getId(), neededMl);
      } catch (Exception e) {
        log.error("Failed to send water reminder for user {}: {}", user.getId(), e.getMessage());
      }
    }

    log.info("[WATER_REMINDER] Finished sending water reminders. Count={}", activeUsers.size());
  }
}
