package com.healthcarenow.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

  // Type of event, e.g., EMERGENCY_FALL, MEDICAL_REMINDER
  private String eventType;

  // Alias for eventType used by mobile app
  private String eventId;

  // Target user
  private String userId;

  // Notification content
  private String title;
  private String content;

  // Priority: HIGH, NORMAL, LOW
  private String priority;

  // Additional parameters like {name}, {location}, or fallback email/token
  private Map<String, Object> payload;

}
