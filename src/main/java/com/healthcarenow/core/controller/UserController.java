package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.DeviceTokenRequest;
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

  @PostMapping("/device-token")
  public ResponseEntity<Void> updateDeviceToken(
      @AuthenticationPrincipal String userId,
      @RequestBody DeviceTokenRequest request) {
    userService.updateDeviceToken(userId, request.getDeviceToken());
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/avatar", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> updateAvatar(
      @AuthenticationPrincipal String userId,
      @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
    return ResponseEntity.ok(userService.updateAvatar(userId, file));
  }

  @PostMapping("/tracking")
  public ResponseEntity<Void> updateTracking(
      @AuthenticationPrincipal String userId,
      @RequestBody com.healthcarenow.core.dto.TrackingRequest request) {
    userService.updateTracking(userId, request);
    return ResponseEntity.ok().build();
  }
}
