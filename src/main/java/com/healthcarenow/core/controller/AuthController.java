package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.AuthRequest;
import com.healthcarenow.core.dto.AuthResponse;
import com.healthcarenow.core.dto.ChangePasswordConfirmRequest;
import com.healthcarenow.core.dto.ChangePasswordRequest;
import com.healthcarenow.core.dto.ForgotPasswordConfirmRequest;
import com.healthcarenow.core.dto.ForgotPasswordRequest;
import com.healthcarenow.core.service.AuthService;
import com.healthcarenow.core.config.JwtTokenProvider;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtTokenProvider tokenProvider;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/google")
  public ResponseEntity<AuthResponse> googleLogin(@RequestBody AuthRequest request) {
    if (!StringUtils.hasText(request.getIdToken())) {
        return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(authService.googleLogin(request));
  }

  @PostMapping("/forgot-password/request-otp")
  public ResponseEntity<Void> requestForgotPasswordOtp(@RequestBody ForgotPasswordRequest request) {
    authService.requestForgotPasswordOtp(request.getEmail());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/forgot-password/confirm")
  public ResponseEntity<Void> confirmForgotPassword(@RequestBody ForgotPasswordConfirmRequest request) {
    authService.confirmForgotPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/change-password/request-otp")
  public ResponseEntity<Void> requestChangePasswordOtp(@RequestBody ChangePasswordRequest request) {
    authService.requestChangePasswordOtp(request.getEmail(), request.getCurrentPassword());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/change-password/confirm")
  public ResponseEntity<Void> confirmChangePassword(@RequestBody ChangePasswordConfirmRequest request) {
    authService.confirmChangePassword(
        request.getEmail(),
        request.getCurrentPassword(),
        request.getOtp(),
        request.getNewPassword());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
    // authService.logout(token);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/validate")
  public ResponseEntity<Void> validateToken(HttpServletRequest request) {
    try {
      String bearerToken = request.getHeader("Authorization");
      if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        String token = bearerToken.substring(7);
        if (tokenProvider.validateToken(token)) {
          String userId = tokenProvider.getUserIdFromJWT(token);
          return ResponseEntity.ok()
              .header("x-user-id", userId)
              .build();
        }
      }
      return ResponseEntity.status(401).build();
    } catch (Exception ex) {
      // Any JWT exception (SignatureException, MalformedJwtException, etc.) should result in 401
      return ResponseEntity.status(401).build();
    }
  }
}
