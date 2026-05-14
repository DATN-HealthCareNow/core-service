package com.healthcarenow.core.dto;

import lombok.Data;

@Data
public class ChangeEmailRequest {
  private String currentEmail;
  private String newEmail;
  private String password; // Optional, for password-based accounts
}
