package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "health_scores")
@Data
public class HealthScore {
  @Id
  private String id;

  @Indexed
  private String userId;

  @Indexed
  private LocalDate date;

  private Double bmi;
  private Integer tdee;
  private LocalDateTime calculatedAt;

  private ScoreDetails healthScore;

  @Data
  public static class ScoreDetails {
    private Integer value;
    private Integer max;
    private String level;
    private Integer activity;
    private Integer sleep;
    private Integer hydration;
    private Integer nutrition;
  }
}
