package com.healthcarenow.core.dto;

import lombok.Data;
import java.util.List;

@Data
public class MedicalRecordDTO {
  private String id;
  private String recordType;
  private String title;
  private String clinicalNotes;
  private List<String> icdCodes;
  // Simplified for MVP
}
