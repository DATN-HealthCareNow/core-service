package com.healthcarenow.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WaterProgressDTO {
  private int totalTodayMl;
  private int goalMl;
  private double progressPercent;
}
