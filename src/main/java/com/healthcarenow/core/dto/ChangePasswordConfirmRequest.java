package com.healthcarenow.core.dto;

import lombok.Data;

@Data
public class ChangePasswordConfirmRequest {
  private String email;
  private String currentPassword;
  private String otp;
  private String newPassword;
}
