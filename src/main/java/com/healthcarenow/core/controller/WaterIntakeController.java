package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.WaterLogRequest;
import com.healthcarenow.core.dto.WaterProgressDTO;
import com.healthcarenow.core.model.mongo.WaterIntake;
import com.healthcarenow.core.service.WaterIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/water-intake")
@RequiredArgsConstructor
public class WaterIntakeController {
  private final WaterIntakeService waterIntakeService;

  @GetMapping("/today")
  public ResponseEntity<WaterIntake> getTodayWaterIntake(@AuthenticationPrincipal String userId) {
      if (userId == null) {
          return ResponseEntity.status(401).build();
      }
      return ResponseEntity.ok(waterIntakeService.getTodayWaterIntake(userId));
  }

  @PostMapping("/log")
  public ResponseEntity<WaterIntake> logWater(@AuthenticationPrincipal String userId,
                                              @RequestBody WaterLogRequest request) {
    // keeping old method just in case, but probably not used if coming through rabbitmq
    return ResponseEntity.ok(waterIntakeService.logWater(userId, request.getAmountMl()));
  }

  @GetMapping("/progress")
  public ResponseEntity<WaterProgressDTO> getProgress(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(waterIntakeService.getProgress(userId));
  }

  @PutMapping("/goal")
  public ResponseEntity<Void> updateGoal(@AuthenticationPrincipal String userId, @RequestBody WaterLogRequest request) {
    waterIntakeService.updateGoal(userId, request.getAmountMl());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/internal/{userId}/progress")
  public ResponseEntity<WaterProgressDTO> getInternalProgress(
      @PathVariable("userId") String userId,
      @RequestHeader(value = "X-Internal-Token", required = false) String token) {
    if (!"hcn-internal-secret-2024".equals(token)) {
      return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(waterIntakeService.getProgress(userId));
  }
}
