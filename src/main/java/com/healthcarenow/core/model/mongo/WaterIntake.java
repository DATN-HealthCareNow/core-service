package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "water_intakes")
@CompoundIndexes({
  @CompoundIndex(name = "water_intake_user_date_unique", def = "{'userId': 1, 'date': 1}", unique = true)
})
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
