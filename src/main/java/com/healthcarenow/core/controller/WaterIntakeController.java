package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.WaterLogRequest;
import com.healthcarenow.core.dto.WaterProgressDTO;
import com.healthcarenow.core.model.mongo.WaterIntake;
import com.healthcarenow.core.service.WaterIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/water")
@RequiredArgsConstructor
public class WaterIntakeController {
  private final WaterIntakeService waterIntakeService;

  @PostMapping("/log")
  public ResponseEntity<WaterIntake> logWater(@RequestHeader("X-User-Id") String userId,
      @RequestBody WaterLogRequest request) {
    return ResponseEntity.ok(waterIntakeService.logWater(userId, request.getAmountMl()));
  }

  @GetMapping("/progress")
  public ResponseEntity<WaterProgressDTO> getProgress(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(waterIntakeService.getProgress(userId));
  }

  @PutMapping("/goal")
  public ResponseEntity<Void> updateGoal(@RequestHeader("X-User-Id") String userId,
      @RequestBody WaterLogRequest request) { // Reusing DTO for amount
    waterIntakeService.updateGoal(userId, request.getAmountMl());
    return ResponseEntity.ok().build();
  }
}
