package com.healthcarenow.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SleepAnalysisDTO {
  private double avgDurationHours;
  private int avgEfficiency;
  private int avgHeartRate;
  private String qualityAssessment;
}
