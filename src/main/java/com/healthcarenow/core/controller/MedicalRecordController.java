package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.MedicalRecordDTO;
import com.healthcarenow.core.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

  private final MedicalRecordService medicalRecordService;

  @PostMapping
  public ResponseEntity<MedicalRecordDTO> createRecord(
      @RequestHeader("X-User-Id") String userId,
      @RequestBody MedicalRecordDTO dto) {
    return ResponseEntity.ok(medicalRecordService.createRecord(userId, dto));
  }

  @GetMapping
  public ResponseEntity<List<MedicalRecordDTO>> getRecords(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(medicalRecordService.getUserRecords(userId));
  }
}
