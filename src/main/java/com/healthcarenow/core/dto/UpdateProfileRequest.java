package com.healthcarenow.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

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
}
