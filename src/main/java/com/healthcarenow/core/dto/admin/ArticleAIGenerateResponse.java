package com.healthcarenow.core.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ArticleAIGenerateResponse {

  private String title;

  private String summary;

  private String content;

  private List<String> seoKeywords;

  private String metaTitle;

  private String metaDescription;

  private String slugSuggestion;
}
