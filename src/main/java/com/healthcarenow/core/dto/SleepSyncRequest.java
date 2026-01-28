package com.healthcarenow.core.dto;

import com.healthcarenow.core.model.mongo.SleepSession;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SleepSyncRequest {
  private LocalDateTime bedtimeStart;
  private LocalDateTime wakeupEnd;
  private SleepSession.SleepStages stages;
  private SleepSession.HeartRate heartRate;
  private String source;
}
