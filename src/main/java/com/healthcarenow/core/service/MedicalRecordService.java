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

    MedicalRecord saved = medicalRecordRepository.save(record);
    dto.setId(saved.getId());
    return dto;
  }

  public List<MedicalRecordDTO> getUserRecords(String userId) {
    return medicalRecordRepository.findByUserId(userId).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  private MedicalRecordDTO mapToDTO(MedicalRecord record) {
    MedicalRecordDTO dto = new MedicalRecordDTO();
    dto.setId(record.getId());
    dto.setRecordType(record.getRecordType());
    dto.setTitle(record.getTitle());
    dto.setClinicalNotes(record.getClinicalNotes());
    dto.setIcdCodes(record.getIcdCodes());
    return dto;
  }
}
