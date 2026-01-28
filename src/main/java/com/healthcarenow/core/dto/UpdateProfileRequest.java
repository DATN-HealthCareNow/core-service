package com.healthcarenow.core.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
  private String fullName;
  private LocalDate dateOfBirth;
  private String gender;
  private Integer heightCm;
  private Integer weightKg;
}
