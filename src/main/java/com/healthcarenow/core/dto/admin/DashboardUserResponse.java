package com.healthcarenow.core.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DashboardUserResponse {
  private String id;
  private String fullName;
  private String email;
  private String role;
  private String status;
  private LocalDate dateOfBirth;
  private Integer heightCm;
  private Integer weightKg;
  private String avatarUrl;
  private LocalDateTime lastLogin;
}