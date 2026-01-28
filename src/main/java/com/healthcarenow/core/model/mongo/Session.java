package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sessions")
@Data
public class Session {
  @Id
  private String id;

  @Indexed
  private String userId;

  private String tokenHash;

  private LocalDateTime createdAt;

  @Indexed(expireAfterSeconds = 0) // TTL index
  private LocalDateTime expiresAt;

  private boolean revoked;

  private DeviceInfo deviceInfo;

  @Data
  public static class DeviceInfo {
    private String userAgent;
    private String ipAddress;
    private String location;
  }
}
