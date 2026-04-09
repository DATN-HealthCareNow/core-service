package com.healthcarenow.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthcarenow.core.dto.WaterProgressDTO;
import com.healthcarenow.core.model.mongo.PatientProfile;
import com.healthcarenow.core.model.mongo.WaterIntake;
import com.healthcarenow.core.repository.mongo.PatientProfileRepository;
import com.healthcarenow.core.repository.mongo.WaterIntakeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaterIntakeService {
  private final WaterIntakeRepository waterIntakeRepository;
  private final PatientProfileRepository patientProfileRepository;

  public void handleWaterLoggedEvent(String userId, Integer amountMl, String dateString) {
      WaterIntake intake = waterIntakeRepository.findFirstByUserIdAndDateStringOrderByTimestampDesc(userId, dateString);
      if (intake == null) {
          intake = getTodayWaterIntake(userId);
          intake.setDateString(dateString);
      }
      
      int currentTotal = intake.getTotalTodayMl() != null ? intake.getTotalTodayMl() : 0;
      intake.setTotalTodayMl(currentTotal + amountMl);
      intake.setAmountMl(amountMl);
      intake.setTimestamp(LocalDateTime.now());
      intake.setDateString(dateString);
      
      if (intake.getGoalMl() != null && intake.getGoalMl() > 0) {
          double progress = (double) intake.getTotalTodayMl() / intake.getGoalMl() * 100.0;
          progress = Math.min(progress, 100.0);
          progress = Math.round(progress * 10.0) / 10.0;
          intake.setProgressPercent(progress);
      }
      
      waterIntakeRepository.save(intake);
  }

  public WaterIntake getTodayWaterIntake(String userId) {
      String todayString = LocalDate.now().toString();
      List<WaterIntake> logs = waterIntakeRepository.findByUserIdAndDateString(userId, todayString);
      if (!logs.isEmpty()) {
          return logs.get(0);
      }
      
      WaterIntake newIntake = new WaterIntake();
      newIntake.setUserId(userId);
      newIntake.setDateString(todayString);
      newIntake.setTimestamp(LocalDateTime.now());
      newIntake.setTotalTodayMl(0);
      newIntake.setAmountMl(0);
      
      double temperature = 25.0;
      double avgExerciseMinutes = 0.0;
      
      try {
          RestTemplate restTemplate = new RestTemplate();
          HttpHeaders headers = new HttpHeaders();
          headers.set("X-Internal-Token", "hcn-internal-secret-2024"); // Correct token for iot-service
          HttpEntity<String> entity = new HttpEntity<>(headers);
          
          ResponseEntity<JsonNode> response = restTemplate.exchange(
                  "http://iot-service:8082/api/v1/internal/water-metrics/" + userId,
                  HttpMethod.GET,
                  entity,
                  JsonNode.class
          );
          
          if (response.getBody() != null) {
              JsonNode body = response.getBody();
              if (body.has("temperature")) {
                  temperature = body.get("temperature").asDouble();
              }
              if (body.has("avgExerciseMinutes")) {
                  avgExerciseMinutes = body.get("avgExerciseMinutes").asDouble();
              }
          }
      } catch (Exception e) {
          log.error("Failed to fetch metrics from iot-service for userId: {}", userId, e);
      }
      
      PatientProfile profile = patientProfileRepository.findByUserId(userId).orElse(null);
      int weightKg = (profile != null && profile.getWeightKg() != null) ? profile.getWeightKg() : 65;
      
      double weightLbs = weightKg / 0.45359237;
      double baseOz = weightLbs * 0.5;
      double exerciseOz = (avgExerciseMinutes / 30.0) * 12.0;
      double totalOz = baseOz + exerciseOz;
      int goalMl = (int) Math.round(totalOz * 29.5735296);
      
      if (temperature > 30.0) {
          goalMl += 500;
      }
      
      newIntake.setGoalMl(goalMl);
      newIntake.setProgressPercent(0.0);
      
      return waterIntakeRepository.save(newIntake);
  }

  public WaterIntake logWater(String userId, int amountMl) {
      handleWaterLoggedEvent(userId, amountMl, LocalDate.now().toString());
      return getTodayWaterIntake(userId);
  }

  public WaterProgressDTO getProgress(String userId) {
      WaterIntake intake = getTodayWaterIntake(userId);
      return WaterProgressDTO.builder()
              .totalTodayMl(intake.getTotalTodayMl() != null ? intake.getTotalTodayMl() : 0)
              .goalMl(intake.getGoalMl() != null ? intake.getGoalMl() : 0)
              .progressPercent(intake.getProgressPercent() != null ? intake.getProgressPercent() : 0.0)
              .build();
  }

  public void updateGoal(String userId, int goalMl) {
      WaterIntake intake = getTodayWaterIntake(userId);
      intake.setGoalMl(goalMl);
      if (goalMl > 0) {
          int total = intake.getTotalTodayMl() != null ? intake.getTotalTodayMl() : 0;
          double progress = (double) total / goalMl * 100.0;
          progress = Math.min(progress, 100.0);
          progress = Math.round(progress * 10.0) / 10.0;
          intake.setProgressPercent(progress);
      }
      waterIntakeRepository.save(intake);
  }
}
