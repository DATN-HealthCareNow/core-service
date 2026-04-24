package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.MedicalRecordDTO;
import com.healthcarenow.core.model.mongo.MedicalRecord;
import com.healthcarenow.core.repository.mongo.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

  private final MedicalRecordRepository medicalRecordRepository;
  private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

  public MedicalRecordDTO createRecord(String userId, MedicalRecordDTO dto) {
    MedicalRecord record = new MedicalRecord();
    record.setUserId(userId);
    record.setRecordType(dto.getRecordType());
    record.setTitle(dto.getTitle());
    record.setClinicalNotes(dto.getClinicalNotes());
    record.setIcdCodes(dto.getIcdCodes());
    
    if (dto.getImageUrl() != null) {
        MedicalRecord.FileMeta fileMeta = new MedicalRecord.FileMeta();
        fileMeta.setS3Url(dto.getImageUrl());
        fileMeta.setFileType("image/jpeg");
        fileMeta.setAiProcessed(true);
        record.setFiles(java.util.List.of(fileMeta));
    }
    
    if (dto.getAiAnalysis() != null) {
        record.setClinicalNotes(record.getClinicalNotes() + "\nAI Analysis: " + dto.getAiAnalysis());
    }

    MedicalRecord saved = medicalRecordRepository.save(record);
    dto.setId(saved.getId());
    return dto;
  }

  public List<MedicalRecordDTO> getUserRecords(String userId) {
    return medicalRecordRepository.findByUserId(userId).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public MedicalRecordDTO getRecord(String id) {
    return medicalRecordRepository.findById(id)
        .map(this::mapToDTO)
        .orElse(null);
  }

  private MedicalRecordDTO mapToDTO(MedicalRecord record) {
    MedicalRecordDTO dto = new MedicalRecordDTO();
    dto.setId(record.getId());
    dto.setRecordType(record.getRecordType());
    dto.setTitle(record.getTitle());
    dto.setDiagnosis(record.getDiagnosis());
    dto.setClinicalNotes(record.getClinicalNotes());
    dto.setIcdCodes(record.getIcdCodes());
    
    if (record.getFiles() != null && !record.getFiles().isEmpty()) {
        dto.setImageUrl(record.getFiles().get(0).getS3Url());
    }
    dto.setForbiddenFoods(record.getForbiddenFoods());
    dto.setMedications(record.getMedications());
    
    if (record.getAiAnalysis() != null) {
        if (record.getAiAnalysis() instanceof String) {
            dto.setAiAnalysis((String) record.getAiAnalysis());
        } else {
            try {
                dto.setAiAnalysis(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(record.getAiAnalysis()));
            } catch (Exception e) {}
        }
    }
    return dto;
  }

  public MedicalRecordDTO updateForbiddenFoods(String id, List<String> foods) {
      org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query(
          org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id)
      );
      org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update()
          .set("forbiddenFoods", foods);
      
      mongoTemplate.updateFirst(query, update, MedicalRecord.class);
      
      return getRecord(id);
  }
}
