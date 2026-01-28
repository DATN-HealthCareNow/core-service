package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "prescriptions")
@Data
public class Prescription {
  @Id
  private String id;

  @Indexed
  private String userId;

  private String medicalRecordId;
  private String prescriptionCode;

  private LocalDateTime issuedDate;
  private LocalDateTime expiryDate;

  private Integer refillsRemaining;
  private String status; // ACTIVE, COMPLETED, EXPIRED

  private List<Medication> medications;

  @Data
  public static class Medication {
    private String name;
    private String genericName;
    private String dosage;
    private String frequency;
    private String route;
    private Integer durationDays;
    private String indication;
    private List<String> sideEffects;
  }
}
