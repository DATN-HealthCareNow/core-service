package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.UserContactResponse;
import com.healthcarenow.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

  private final UserService userService;
  
  @Value("${app.internal-token:hcn-internal-secret-2024}")
  private String internalApiToken;

  private void validateInternalToken(String token) {
    if (token == null || !token.equals(internalApiToken)) {
      throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Internal Token");
    }
  }

  @GetMapping("/{userId}/contact")
  public ResponseEntity<UserContactResponse> getContactInfo(
      @RequestHeader(value = "X-Internal-Token", required = false) String token,
      @PathVariable String userId) {
    validateInternalToken(token);
    return ResponseEntity.ok(userService.getContactInfo(userId));
  }

  @DeleteMapping("/{userId}/device-token")
  public ResponseEntity<Void> removeDeviceToken(
      @RequestHeader(value = "X-Internal-Token", required = false) String token,
      @PathVariable String userId) {
    validateInternalToken(token);
    userService.removeDeviceToken(userId);
    return ResponseEntity.ok().build();
  }
}
