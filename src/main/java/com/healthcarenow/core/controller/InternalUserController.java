package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.UserContactResponse;
import com.healthcarenow.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

  private final UserService userService;

  // Simple static token for internal communication to prevent external access
  // In production, NGINX is configured to block /api/v1/internal/* from outside
  private static final String INTERNAL_API_TOKEN = "hcn-internal-secret-2024";

  private void validateInternalToken(String token) {
    if (token == null || !token.equals(INTERNAL_API_TOKEN)) {
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
