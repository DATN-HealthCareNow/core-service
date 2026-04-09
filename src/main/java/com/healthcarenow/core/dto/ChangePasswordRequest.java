package com.healthcarenow.core.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
  private String email;
  private String currentPassword;
}
