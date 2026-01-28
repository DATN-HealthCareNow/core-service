package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "water_intakes")
@Data
public class WaterIntake {
  @Id
  private String id;

  @Indexed
  private String userId;

  private LocalDateTime timestamp;
  private Integer amountMl;
  private LocalDate date;

  private Integer totalTodayMl;
  private Integer goalMl;
  private Double progressPercent;
  private String adjustmentReason;
}
