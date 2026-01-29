package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.UpdateProfileRequest;
import com.healthcarenow.core.dto.UserProfileResponse;
import com.healthcarenow.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/profile")
  public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal String userId) {
    // userId from JWT injected by Spring Security
    return ResponseEntity.ok(userService.getProfile(userId));
  }

  @PutMapping("/profile")
  public ResponseEntity<UserProfileResponse> updateProfile(
      @AuthenticationPrincipal String userId,
      @RequestBody UpdateProfileRequest request) {

    return ResponseEntity.ok(userService.updateProfile(userId, request));
  }
}
