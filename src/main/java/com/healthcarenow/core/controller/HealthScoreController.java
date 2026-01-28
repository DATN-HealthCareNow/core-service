package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.HealthScoreSummaryDTO;
import com.healthcarenow.core.model.mongo.HealthScore;
import com.healthcarenow.core.service.HealthScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthScoreController {
  private final HealthScoreService healthScoreService;

  @GetMapping("/scores/history")
  public ResponseEntity<List<HealthScore>> getHistory(@RequestHeader("X-User-Id") String userId) {
    // Mock implementation for history list
    return ResponseEntity.ok(List.of());
  }

  @GetMapping("/summary/today")
  public ResponseEntity<HealthScoreSummaryDTO> getTodaySummary(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(healthScoreService.getTodaySummary(userId));
  }

  @PostMapping("/calculate")
  public ResponseEntity<Void> calculateScore(@RequestParam String userId) {
    healthScoreService.calculateScore(userId);
    return ResponseEntity.ok().build();
  }
}
