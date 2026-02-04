package com.healthcarenow.core.model.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
public class User {

  @Id
  private String id; // Automatically generated ObjectId

  private String email;

  private String passwordHash;

  private Role role;

  private String status; // ACTIVE, SUSPENDED, DELETED

  private LocalDateTime deletedAt;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;
}
