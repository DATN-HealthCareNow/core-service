package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.HealthScoreSummaryDTO;
import com.healthcarenow.core.model.mongo.HealthScore;
import com.healthcarenow.core.repository.mongo.HealthScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthScoreService {
  private final HealthScoreRepository healthScoreRepository;

  public HealthScore getLatestScore(String userId) {
    return healthScoreRepository.findTopByUserIdOrderByDateDesc(userId).orElse(null);
  }

  public HealthScoreSummaryDTO getTodaySummary(String userId) {
    HealthScore score = getLatestScore(userId);
    if (score == null)
      return null;

    HealthScoreSummaryDTO dto = new HealthScoreSummaryDTO();
    dto.setBmi(score.getBmi());
    dto.setTdee(score.getTdee());
    dto.setDailyScore(score.getHealthScore());
    return dto;
  }

  // Stub for internal calculation logic
  public void calculateScore(String userId) {
    // Logic to trigger AI or aggregate data goes here
  }
}
