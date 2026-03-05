package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.AuthRequest;
import com.healthcarenow.core.dto.AuthResponse;
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

  @DeleteMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
    // authService.logout(token);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/validate")
  public ResponseEntity<Void> validateToken(HttpServletRequest request) {
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
  }
}
