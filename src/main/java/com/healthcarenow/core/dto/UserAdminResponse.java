package com.healthcarenow.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminResponse {
  private String id;
  private String email;
  private String fullName;
  private String role;
  private String status;
  private java.time.LocalDate dateOfBirth;
  private Double heightCm;
  private Double weightKg;
  private String avatarUrl;
  private LocalDateTime createdAt;
}
