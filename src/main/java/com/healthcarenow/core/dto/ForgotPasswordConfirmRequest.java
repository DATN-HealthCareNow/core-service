package com.healthcarenow.core.dto;

import lombok.Data;

@Data
public class ForgotPasswordConfirmRequest {
  private String email;
  private String otp;
  private String newPassword;
}
