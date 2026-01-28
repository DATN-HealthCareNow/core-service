package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "patient_profiles")
@Data
public class PatientProfile {
  @Id
  private String id; // Matches User.id

  @Indexed(unique = true)
  private String userId; // Redundant but good for indexing/querying clarity

  private String fullName;
  private LocalDate dateOfBirth;
  private String gender;

  private Integer heightCm;
  private Integer weightKg;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private List<EmergencyContact> emergencyContacts;

  private PrivacySettings privacySettings;

  @Data
  public static class EmergencyContact {
    private String name;
    private String email;
    private String phone;
    private Integer priority;
  }

  @Data
  public static class PrivacySettings {
    private boolean dataSharing = false;
    private boolean marketingEmails = false;
  }
}
