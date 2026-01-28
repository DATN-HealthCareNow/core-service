package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.SleepAnalysisDTO;
import com.healthcarenow.core.dto.SleepSyncRequest;
import com.healthcarenow.core.model.mongo.SleepSession;
import com.healthcarenow.core.service.SleepSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sleep")
@RequiredArgsConstructor
public class SleepSessionController {
  private final SleepSessionService sleepSessionService;

  @PostMapping("/sync")
  public ResponseEntity<SleepSession> syncSleep(@RequestHeader("X-User-Id") String userId,
      @RequestBody SleepSyncRequest request) {
    return ResponseEntity.ok(sleepSessionService.syncSleep(userId, request));
  }

  @GetMapping("/analysis")
  public ResponseEntity<SleepAnalysisDTO> getAnalysis(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(sleepSessionService.analyzeSleep(userId));
  }

  @PutMapping("/schedule")
  public ResponseEntity<Void> updateSchedule(@RequestHeader("X-User-Id") String userId) {
    // Implementation for scheduling (db update) would go here
    return ResponseEntity.ok().build();
  }
}
