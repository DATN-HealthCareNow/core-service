package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.UpdateProfileRequest;
import com.healthcarenow.core.dto.UserProfileResponse;
import com.healthcarenow.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // In a real app, userId should be extracted from the JWT token in
  // SecurityContext
  // For this prototype/Postman test, we will accept it as a Header/Param or Mock
  // it
  // Let's assume the user passes "X-User-Id" header for now to test easily.

  @GetMapping("/profile")
  public ResponseEntity<UserProfileResponse> getProfile(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(userService.getProfile(userId));
  }

  @PutMapping("/profile")
  public ResponseEntity<UserProfileResponse> updateProfile(
      @RequestHeader("X-User-Id") String userId,
      @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(userService.updateProfile(userId, request));
  }
}
