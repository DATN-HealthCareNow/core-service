package com.healthcarenow.core.dto;

import com.healthcarenow.core.model.mongo.HealthScore;
import lombok.Data;

@Data
public class HealthScoreSummaryDTO {
  private Double bmi;
  private Integer tdee;
  private HealthScore.ScoreDetails dailyScore;
}
