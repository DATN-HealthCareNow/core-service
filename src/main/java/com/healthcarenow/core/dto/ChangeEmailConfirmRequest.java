package com.healthcarenow.core.dto;

import lombok.Data;

@Data
public class ChangeEmailConfirmRequest {
  private String currentEmail;
  private String newEmail;
  private String otp;
}
