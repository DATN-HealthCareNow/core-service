package com.healthcarenow.core.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleUpsertRequest {

  @NotBlank
  private String title;

  @NotBlank
  private String category;

  private String status; // DRAFT, REVIEW, SCHEDULED, PUBLISHED

  private String summary;

  private String content;

  private List<String> seoKeywords;

  private String metaTitle;

  private String metaDescription;

  private String coverImageUrl;

  private Boolean aiGenerated;

  private String authorId;

  private String authorName;

  private LocalDateTime scheduledAt;
}
