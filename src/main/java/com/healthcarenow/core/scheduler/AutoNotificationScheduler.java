package com.healthcarenow.core.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthcarenow.core.dto.NotificationEvent;
import com.healthcarenow.core.model.mongo.PatientProfile;
import com.healthcarenow.core.model.mongo.WaterIntake;
import com.healthcarenow.core.repository.mongo.PatientProfileRepository;
import com.healthcarenow.core.repository.mongo.WaterIntakeRepository;
import com.healthcarenow.core.service.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoNotificationScheduler {

    private final PatientProfileRepository patientProfileRepository;
    private final WaterIntakeRepository waterIntakeRepository;
    private final NotificationProducer notificationProducer;

    // Chạy lúc 7:00, 12:00, 18:00 mỗi ngày
    @Scheduled(cron = "0 0 7,10,12,18 * * *", zone = "Asia/Ho_Chi_Minh")
    public void scheduleWaterReminders() {
        log.info("[AutoNotification] Starting water reminder job at {}", java.time.LocalTime.now());

        List<PatientProfile> profiles = patientProfileRepository.findAll();
        for (PatientProfile profile : profiles) {
            try {
                String userId = profile.getUserId() != null ? profile.getUserId() : profile.getId();
                if (userId == null)
                    continue;

                String todayString = LocalDate.now().toString();
                List<WaterIntake> logs = waterIntakeRepository.findByUserIdAndDateString(userId, todayString);
                
                // Nếu không có log nào tức là người dùng chưa đăng nhập hôm nay -> bỏ qua tránh tạo rác
                if (logs.isEmpty()) {
                    continue;
                }
                
                WaterIntake intake = logs.get(0);
                int totalToday = intake.getTotalTodayMl() != null ? intake.getTotalTodayMl() : 0;
                int goal = intake.getGoalMl() != null ? intake.getGoalMl() : 0;

                if (goal > 0 && totalToday < goal) {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("current", totalToday);
                    payload.put("goal", goal);
                    payload.put("needed", goal - totalToday);

                    NotificationEvent event = NotificationEvent.builder()
                            .eventType("WATER_REMINDER")
                            .userId(userId)
                            .priority("NORMAL")
                            .payload(payload)
                            .build();

                    notificationProducer.sendNotification(event);
                    log.info("[AutoNotification] Sent water reminder to user {}", userId);
                }
            } catch (Exception e) {
                log.error("[AutoNotification] Failed to process water reminder for profile {}", profile.getId(), e);
            }
        }
    }

    // Chạy lúc 7:00 sáng mỗi ngày để kiểm tra thời gian luyện tập hôm qua
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Ho_Chi_Minh")
    public void scheduleLowExerciseReminders() {
        log.info("[AutoNotification] Starting low exercise reminder job at {}", java.time.LocalTime.now());

        List<PatientProfile> profiles = patientProfileRepository.findAll();
        for (PatientProfile profile : profiles) {
            try {
                String userId = profile.getUserId() != null ? profile.getUserId() : profile.getId();
                if (userId == null)
                    continue;

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Internal-Token", "hcn-internal-secret-2024");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                String yesterday = LocalDate.now().minusDays(1).toString();

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        "http://iot-service:8082/api/v1/internal/exercise-metrics/" + userId + "?date=" + yesterday,
                        HttpMethod.GET,
                        entity,
                        JsonNode.class);

                if (response.getBody() != null) {
                    JsonNode body = response.getBody();
                    boolean belowTarget = body.has("belowTarget") && body.get("belowTarget").asBoolean();

                    if (belowTarget) {
                        int exerciseMinutes = body.has("exerciseMinutes") ? body.get("exerciseMinutes").asInt() : 0;
                        int targetMinutes = 30; // Mặc định 30 phút
                        int missingMinutes = targetMinutes - exerciseMinutes;

                        Map<String, Object> payload = new HashMap<>();
                        payload.put("exercise_minutes", exerciseMinutes);
                        payload.put("target_minutes", targetMinutes);
                        payload.put("missing_minutes", missingMinutes);
                        // Cho template ACTIVITY_REMINDER có sử dụng tên biến khác
                        payload.put("exerciseMinutes", exerciseMinutes);

                        NotificationEvent event = NotificationEvent.builder()
                                .eventType("LOW_EXERCISE_REMINDER")
                                .userId(userId)
                                .priority("NORMAL")
                                .payload(payload)
                                .build();

                        notificationProducer.sendNotification(event);
                        log.info("[AutoNotification] Sent low exercise reminder to user {}", userId);
                    }
                }
            } catch (Exception e) {
                log.error("[AutoNotification] Failed to process low exercise reminder for profile {}", profile.getId(),
                        e);
            }
        }
    }
}
