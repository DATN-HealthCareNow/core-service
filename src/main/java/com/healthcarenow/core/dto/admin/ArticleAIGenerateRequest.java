package com.healthcarenow.core.dto.admin;

import lombok.Data;

import java.util.List;

@Data
public class ArticleAIGenerateRequest {

  private String topic;

  private String category;

  private String tone;

  private String targetAudience;

  private Integer targetWords;

  private List<String> seoKeywords;

  private String callToAction;
}
