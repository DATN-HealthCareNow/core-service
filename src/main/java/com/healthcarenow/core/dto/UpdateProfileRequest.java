package com.healthcarenow.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProfileRequest {
  @JsonProperty("full_name")
  private String fullName;
  @JsonProperty("date_of_birth")
  private LocalDate dateOfBirth;
  private String gender;
  @JsonProperty("height")
  private Integer height;
  @JsonProperty("weight")
  private Integer weight;

  @JsonProperty("medical_history")
  private List<String> medicalHistory;
  
  @JsonProperty("forbidden_foods")
  private List<String> forbiddenFoods;

  @JsonProperty("source_id")
  private String sourceId;
}
