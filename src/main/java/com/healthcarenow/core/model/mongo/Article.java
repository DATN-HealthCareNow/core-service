package com.healthcarenow.core.model.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "articles")
@Data
@NoArgsConstructor
public class Article {

  @Id
  private String id;

  private String title;

  private String slug;

  private String summary;

  private String content;

  private String category;

  private String status; // DRAFT, REVIEW, SCHEDULED, PUBLISHED

  private List<String> seoKeywords;

  private String metaTitle;

  private String metaDescription;

  private String coverImageUrl;

  private boolean aiGenerated;

  private String authorId;

  private String authorName;

  private Long views;

  private LocalDateTime scheduledAt;

  private LocalDateTime publishedAt;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;
}
