package com.healthcarenow.core.dto;

import lombok.Data;
import java.util.List;

@Data
public class MedicalRecordDTO {
  private String id;
  private String recordType;
  private String title;
  private String diagnosis;
  private String clinicalNotes;
  private List<String> icdCodes;
  private List<String> forbiddenFoods;
  private List<Object> medications;
  private String imageUrl;
  private String aiAnalysis; // JSON string of the analysis result
}
