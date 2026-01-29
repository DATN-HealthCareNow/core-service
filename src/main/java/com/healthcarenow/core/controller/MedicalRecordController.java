package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.MedicalRecordDTO;
import com.healthcarenow.core.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

  @GetMapping("/{id}")
  public ResponseEntity<MedicalRecordDTO> getRecord(@PathVariable String id) {
    return ResponseEntity.ok(medicalRecordService.getRecord(id));
  }
}
