package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.WaterProgressDTO;
import com.healthcarenow.core.model.mongo.WaterIntake;
import com.healthcarenow.core.repository.mongo.WaterIntakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaterIntakeService {
  private final WaterIntakeRepository waterIntakeRepository;
  private static final int DEFAULT_GOAL = 2000;

  public WaterIntake logWater(String userId, int amountMl) {
    WaterIntake intake = new WaterIntake();
    intake.setUserId(userId);
    intake.setAmountMl(amountMl);
    intake.setTimestamp(LocalDateTime.now());
    intake.setDate(LocalDate.now());

    // Recalculate totals (simplified logic)
    WaterIntake saved = waterIntakeRepository.save(intake);
    return saved;
  }

  public WaterProgressDTO getProgress(String userId) {
    List<WaterIntake> todayLogs = waterIntakeRepository.findByUserIdAndDate(userId, LocalDate.now());
    int total = todayLogs.stream().mapToInt(WaterIntake::getAmountMl).sum();

    // Find latest goal if any, or use default
    int goal = todayLogs.isEmpty() ? DEFAULT_GOAL
        : todayLogs.stream().max(Comparator.comparing(WaterIntake::getTimestamp))
            .map(log -> log.getGoalMl() != null ? log.getGoalMl() : DEFAULT_GOAL)
            .orElse(DEFAULT_GOAL);

    return WaterProgressDTO.builder()
        .totalTodayMl(total)
        .goalMl(goal)
        .progressPercent(goal > 0 ? (double) total / goal * 100 : 0)
        .build();
  }

  public void updateGoal(String userId, int newGoal) {
    // Logic to update goal for today involves updating the latest record or user
    // settings
    // Simplified: future logs will need to reflect this.
  }
}
