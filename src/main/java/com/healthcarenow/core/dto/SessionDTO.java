package com.healthcarenow.core.dto;

import com.healthcarenow.core.model.mongo.Session;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionDTO {
  private String id;
  private String userAgent;
  private String ipAddress;
  private LocalDateTime createdAt;
  private boolean isCurrent;
}
