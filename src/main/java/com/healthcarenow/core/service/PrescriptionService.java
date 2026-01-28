package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.PrescriptionDTO;
import com.healthcarenow.core.model.mongo.Prescription;
import com.healthcarenow.core.repository.mongo.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
  private final PrescriptionRepository prescriptionRepository;

  public List<PrescriptionDTO> getActivePrescriptions(String userId) {
    return prescriptionRepository.findByUserId(userId).stream()
        .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public void refillPrescription(String prescriptionId) {
    prescriptionRepository.findById(prescriptionId).ifPresent(p -> {
      if (p.getRefillsRemaining() > 0) {
        p.setRefillsRemaining(p.getRefillsRemaining() - 1);
        prescriptionRepository.save(p);
      }
    });
  }

  private PrescriptionDTO mapToDTO(Prescription p) {
    PrescriptionDTO dto = new PrescriptionDTO();
    dto.setId(p.getId());
    dto.setPrescriptionCode(p.getPrescriptionCode());
    dto.setIssuedDate(p.getIssuedDate());
    dto.setExpiryDate(p.getExpiryDate());
    dto.setRefillsRemaining(p.getRefillsRemaining());
    dto.setStatus(p.getStatus());
    dto.setMedications(p.getMedications());
    return dto;
  }
}
