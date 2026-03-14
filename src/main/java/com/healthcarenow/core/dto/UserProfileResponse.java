package com.healthcarenow.core.dto;

import com.healthcarenow.core.model.mongo.PatientProfile;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
  private String id;
  private String email;
  @JsonProperty("full_name")
  private String fullName;
  @JsonProperty("date_of_birth")
  private LocalDate dateOfBirth;
  private String gender;
  @JsonProperty("height")
  private Integer height;
  @JsonProperty("weight")
  private Integer weight;
  @JsonProperty("avatar_url")
  private String avatarUrl;
  private PatientProfile.PrivacySettings privacySettings;
}
