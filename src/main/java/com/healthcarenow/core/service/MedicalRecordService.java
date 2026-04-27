package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.MedicalRecordDTO;
import com.healthcarenow.core.model.mongo.MedicalRecord;
import com.healthcarenow.core.repository.mongo.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    record.setDiagnosis(dto.getDiagnosis());
    record.setClinicalNotes(dto.getClinicalNotes());
    record.setIcdCodes(dto.getIcdCodes());
    record.setForbiddenFoods(dto.getForbiddenFoods());
    record.setMedications(dto.getMedications());
    record.setStatus("ACTIVE");

    LocalDateTime now = LocalDateTime.now();
    record.setCreatedAt(now);
    record.setUpdatedAt(now);

    // ── Tự động tính expiryDate từ duration_days của thuốc ──────────────────
    // Ưu tiên: giá trị user truyền lên → tính từ medications → default 30 ngày
    if (dto.getExpiryDate() != null) {
        record.setExpiryDate(dto.getExpiryDate());
    } else {
        int maxDays = extractMaxDurationDays(dto.getMedications());
        // Nếu không có thông tin duration → default 30 ngày
        record.setExpiryDate(now.plusDays(maxDays > 0 ? maxDays : 30));
    }
    // ─────────────────────────────────────────────────────────────────────────

    if (dto.getImageUrl() != null) {
        MedicalRecord.FileMeta fileMeta = new MedicalRecord.FileMeta();
        fileMeta.setS3Url(dto.getImageUrl());
        fileMeta.setFileType("image/jpeg");
        fileMeta.setAiProcessed(true);
        record.setFiles(java.util.List.of(fileMeta));
    }

    if (dto.getAiAnalysis() != null) {
        record.setAiAnalysis(dto.getAiAnalysis());
    }

    MedicalRecord saved = medicalRecordRepository.save(record);
    return mapToDTO(saved);
  }

  /**
   * Tìm duration_days lớn nhất trong danh sách medications.
   * AI trả về nhiều key khác nhau nên phải thử nhiều tên field.
   */
  private int extractMaxDurationDays(List<Object> medications) {
    if (medications == null || medications.isEmpty()) return 0;
    int max = 0;
    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    for (Object med : medications) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = mapper.valueToTree(med);
            // Thử các key phổ biến mà AI có thể trả về
            for (String key : new String[]{"duration_days", "durationDays", "duration_day", "days"}) {
                if (node.has(key) && node.get(key).isNumber()) {
                    int days = node.get(key).asInt(0);
                    if (days > max) max = days;
                    break;
                }
            }
            // Nếu không có duration_days, thử parse từ field "duration" dạng "14 ngày" / "2 weeks"
            if (max == 0 && node.has("duration")) {
                String dur = node.get("duration").asText("");
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(dur);
                if (m.find()) {
                    int val = Integer.parseInt(m.group(1));
                    // Nếu đơn vị là tuần → nhân 7
                    if (dur.toLowerCase().contains("week") || dur.toLowerCase().contains("tuần")) val *= 7;
                    if (val > max) max = val;
                }
            }
        } catch (Exception ignored) {}
    }
    return max;
  }

  public List<MedicalRecordDTO> getUserRecords(String userId) {
    return medicalRecordRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  /** Chỉ lấy các record đang ACTIVE — dùng cho AI meal plan.
   * Tự động lọc: status == ACTIVE VÀ (expiryDate == null HOẶC expiryDate > now())
   */
  public List<MedicalRecordDTO> getActiveRecords(String userId) {
    LocalDateTime now = LocalDateTime.now();
    org.springframework.data.mongodb.core.query.Query query =
        new org.springframework.data.mongodb.core.query.Query(
            org.springframework.data.mongodb.core.query.Criteria.where("userId").is(userId)
                .andOperator(
                    new org.springframework.data.mongodb.core.query.Criteria().orOperator(
                        org.springframework.data.mongodb.core.query.Criteria.where("status").is("ACTIVE"),
                        org.springframework.data.mongodb.core.query.Criteria.where("status").exists(false),
                        org.springframework.data.mongodb.core.query.Criteria.where("status").is(null)
                    ),
                    new org.springframework.data.mongodb.core.query.Criteria().orOperator(
                        org.springframework.data.mongodb.core.query.Criteria.where("expiryDate").exists(false),
                        org.springframework.data.mongodb.core.query.Criteria.where("expiryDate").is(null),
                        org.springframework.data.mongodb.core.query.Criteria.where("expiryDate").gt(now)
                    )
                )
        ).with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

    return mongoTemplate.find(query, MedicalRecord.class).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public MedicalRecordDTO getRecord(String id) {
    return medicalRecordRepository.findById(id)
        .map(this::mapToDTO)
        .orElse(null);
  }

  /** Cập nhật trạng thái (ACTIVE / EXPIRED) và expiryDate */
  public MedicalRecordDTO updateStatus(String id, String status, LocalDateTime expiryDate) {
      org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query(
          org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id)
      );
      org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update()
          .set("status", status)
          .set("updatedAt", LocalDateTime.now());
      if (expiryDate != null) {
          update.set("expiryDate", expiryDate);
      }
      mongoTemplate.updateFirst(query, update, MedicalRecord.class);
      return getRecord(id);
  }

  public MedicalRecordDTO updateForbiddenFoods(String id, List<String> foods) {
      org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query(
          org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id)
      );
      org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update()
          .set("forbiddenFoods", foods)
          .set("updatedAt", LocalDateTime.now());
      mongoTemplate.updateFirst(query, update, MedicalRecord.class);
      return getRecord(id);
  }

  private MedicalRecordDTO mapToDTO(MedicalRecord record) {
    MedicalRecordDTO dto = new MedicalRecordDTO();
    dto.setId(record.getId());
    dto.setRecordType(record.getRecordType());
    dto.setTitle(record.getTitle());
    dto.setDiagnosis(record.getDiagnosis());
    dto.setClinicalNotes(record.getClinicalNotes());
    dto.setIcdCodes(record.getIcdCodes());
    dto.setForbiddenFoods(record.getForbiddenFoods());
    dto.setMedications(record.getMedications());
    dto.setExpiryDate(record.getExpiryDate());
    dto.setStatus(record.getStatus() != null ? record.getStatus() : "ACTIVE");
    dto.setCreatedAt(record.getCreatedAt());
    dto.setUpdatedAt(record.getUpdatedAt());

    if (record.getFiles() != null && !record.getFiles().isEmpty()) {
        dto.setImageUrl(record.getFiles().get(0).getS3Url());
    }

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
}
