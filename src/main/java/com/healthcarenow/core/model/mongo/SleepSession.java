package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sleep_sessions")
@Data
public class SleepSession {
  @Id
  private String id;

  @Indexed
  private String userId;

  private String mode;
  private LocalDateTime bedtimeStart;
  private LocalDateTime wakeupEnd;
  private Double durationHours;
  private Integer efficiencyPercent;
  private String source; // e.g., HEALTH_KIT

  private SleepStages stages;
  private HeartRate heartRate;

  @Data
  public static class SleepStages {
    private Integer deepMinutes;
    private Integer lightMinutes;
    private Integer remMinutes;
    private Integer awakeMinutes;
  }

  @Data
  public static class HeartRate {
    private Integer avg;
    private Integer min;
    private Integer max;
  }
}
