package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.PrescriptionDTO;
import com.healthcarenow.core.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medical/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
  private final PrescriptionService prescriptionService;

  @GetMapping
  public ResponseEntity<List<PrescriptionDTO>> getActivePrescriptions(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(prescriptionService.getActivePrescriptions(userId));
  }

  @PatchMapping("/{id}/refill")
  public ResponseEntity<Void> refillPrescription(@PathVariable String id) {
    prescriptionService.refillPrescription(id);
    return ResponseEntity.ok().build();
  }
}
