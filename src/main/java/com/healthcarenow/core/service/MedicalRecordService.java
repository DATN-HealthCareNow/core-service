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
    dto.setClinicalNotes(record.getClinicalNotes());
    dto.setIcdCodes(record.getIcdCodes());
    
    if (record.getFiles() != null && !record.getFiles().isEmpty()) {
        dto.setImageUrl(record.getFiles().get(0).getS3Url());
    }
    dto.setForbiddenFoods(record.getForbiddenFoods());
    return dto;
  }

  public MedicalRecordDTO updateForbiddenFoods(String id, List<String> foods) {
      MedicalRecord record = medicalRecordRepository.findById(id).orElseThrow();
      record.setForbiddenFoods(foods);
      return mapToDTO(medicalRecordRepository.save(record));
  }
}
