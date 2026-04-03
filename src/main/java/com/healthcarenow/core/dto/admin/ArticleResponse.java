package com.healthcarenow.core.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ArticleResponse {

  private String id;

  private String title;

  private String slug;

  private String category;

  private String summary;

  private String content;

  private String status;

  private List<String> seoKeywords;

  private String metaTitle;

  private String metaDescription;

  private String coverImageUrl;

  private boolean aiGenerated;

  private String authorName;

  private long views;

  private LocalDateTime scheduledAt;

  private LocalDateTime publishedAt;

  private LocalDateTime updatedAt;
}
