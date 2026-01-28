package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "medical_records")
@Data
public class MedicalRecord {
  @Id
  private String id;

  @Indexed
  private String userId;

  private String recordType; // e.g., LAB, DIAGNOSIS
  private String title;
  private String clinicalNotes;
  private List<String> icdCodes;

  private AuditLog auditLog;
  private Metadata metadata;
  private List<FileMeta> files;
  private Sharing sharing;

  @Data
  public static class Sharing {
    private List<SharedWith> sharedWith;
    private boolean requiresConsent;
  }

  @Data
  public static class SharedWith {
    private String userId;
    private String access; // VIEW, EDIT
  }

  @Data
  public static class AuditLog {
    private String createdBy;
    private LocalDateTime lastAccessedAt;
    private Integer accessCount;
  }

  @Data
  public static class Metadata {
    private String doctorName;
    private String doctorLicense;
    private String clinic;
    private String clinicAddress;
    private LocalDateTime date;
    private LocalDateTime nextAppointment;
  }

  @Data
  public static class FileMeta {
    private String fileId;
    private String filename;
    private String s3Url;
    private String fileType;
    private Long sizeBytes;
    private LocalDateTime uploadedAt;
    private boolean aiProcessed;
    private String aiAnalysisId;
  }
}
