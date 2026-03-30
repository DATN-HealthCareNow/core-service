package com.healthcarenow.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WaterLogRequest {
  @JsonProperty("amount_ml")
  private int amountMl;
}
