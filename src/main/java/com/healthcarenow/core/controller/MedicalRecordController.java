package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.MedicalRecordDTO;
import com.healthcarenow.core.service.MedicalRecordService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

  private final MedicalRecordService medicalRecordService;

  @PostMapping
  public ResponseEntity<MedicalRecordDTO> createRecord(
      @AuthenticationPrincipal String userId,
      @RequestBody MedicalRecordDTO request) {
    return ResponseEntity.ok(medicalRecordService.createRecord(userId, request));
  }

  @GetMapping
  public ResponseEntity<List<MedicalRecordDTO>> getUserRecords(
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(medicalRecordService.getUserRecords(userId));
  }

  /** Chỉ trả về record đang ACTIVE — dùng cho AI meal plan */
  @GetMapping("/active")
  public ResponseEntity<List<MedicalRecordDTO>> getActiveRecords(
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(medicalRecordService.getActiveRecords(userId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<MedicalRecordDTO> getRecord(@PathVariable String id) {
    return ResponseEntity.ok(medicalRecordService.getRecord(id));
  }

  @PutMapping("/{id}/forbidden-foods")
  public ResponseEntity<MedicalRecordDTO> updateForbiddenFoods(
      @PathVariable String id,
      @RequestBody List<String> foods) {
    return ResponseEntity.ok(medicalRecordService.updateForbiddenFoods(id, foods));
  }

  /** Cập nhật trạng thái ACTIVE / EXPIRED */
  @PostMapping("/{id}/status")
  public ResponseEntity<MedicalRecordDTO> updateStatus(
      @PathVariable String id,
      @RequestBody UpdateStatusRequest request) {
    return ResponseEntity.ok(
        medicalRecordService.updateStatus(id, request.getStatus(), request.getExpiryDate()));
  }

  @Data
  public static class UpdateStatusRequest {
    private String status;          // "ACTIVE" | "EXPIRED"
    private LocalDateTime expiryDate; // optional
  }
}
