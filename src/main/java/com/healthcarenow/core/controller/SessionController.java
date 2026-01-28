package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.SessionDTO;
import com.healthcarenow.core.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
public class SessionController {
  private final SessionService sessionService;

  @GetMapping("/active")
  public ResponseEntity<List<SessionDTO>> getActiveSessions(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(sessionService.getActiveSessions(userId));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> revokeSession(@PathVariable String id) {
    sessionService.revokeSession(id);
    return ResponseEntity.ok().build();
  }
}
