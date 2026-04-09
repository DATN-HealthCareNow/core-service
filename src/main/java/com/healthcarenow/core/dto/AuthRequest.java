package com.healthcarenow.core.dto;

import lombok.Data;

@Data
public class AuthRequest {
  private String email;
  private String password;
  @com.fasterxml.jackson.annotation.JsonProperty("full_name")
  private String fullName;

  private String idToken;
}
