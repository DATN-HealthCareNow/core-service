package com.healthcarenow.core.dto;

import lombok.Data;

@Data
public class AuthRequest {
  private String email;
  private String password;
  private String fullName; // Optional for login
}
