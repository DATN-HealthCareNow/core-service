package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.ChangeRoleRequest;
import com.healthcarenow.core.dto.UserAdminResponse;
import com.healthcarenow.core.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminUserController {
  
  private final AdminUserService adminUserService;
  @GetMapping
  public ResponseEntity<List<UserAdminResponse>> getAllUsers(@AuthenticationPrincipal String adminId) {
    return ResponseEntity.ok(adminUserService.getAllUsers(adminId));
  }

  @PutMapping("/{userId}/role")
  public ResponseEntity<UserAdminResponse> changeRole(
      @AuthenticationPrincipal String adminId,
      @PathVariable String userId,
      @RequestBody ChangeRoleRequest request) {
    return ResponseEntity.ok(adminUserService.changeRole(adminId, userId, request.getRole()));
  }

  @PutMapping("/{userId}/status")
  public ResponseEntity<UserAdminResponse> changeStatus(
      @AuthenticationPrincipal String adminId,
      @PathVariable String userId,
      @RequestBody java.util.Map<String, String> request) {
    return ResponseEntity.ok(adminUserService.changeStatus(adminId, userId, request.get("status")));
  }
}
