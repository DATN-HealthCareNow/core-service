package com.healthcarenow.core.dto;

import com.healthcarenow.core.model.mongo.Prescription;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionDTO {
  private String id;
  private String prescriptionCode;
  private LocalDateTime issuedDate;
  private LocalDateTime expiryDate;
  private Integer refillsRemaining;
  private String status;
  private List<Prescription.Medication> medications;
}
