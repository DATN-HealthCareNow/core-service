package com.healthcarenow.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MealMacroDTO {
  private double totalCalories;
  private double proteinG;
  private double fatG;
  private double carbsG;
  private double fiberG;
}
