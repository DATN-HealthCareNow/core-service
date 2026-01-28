package com.healthcarenow.core.dto;

import com.healthcarenow.core.model.mongo.PatientProfile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
  private String id;
  private String email;
  private String fullName;
  private LocalDate dateOfBirth;
  private String gender;
  private Integer heightCm;
  private Integer weightKg;
  private PatientProfile.PrivacySettings privacySettings;
}
