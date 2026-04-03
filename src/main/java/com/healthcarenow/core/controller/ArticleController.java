package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.admin.ArticleResponse;
import com.healthcarenow.core.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;

  @GetMapping
  public ResponseEntity<List<ArticleResponse>> getPublishedArticles() {
    return ResponseEntity.ok(articleService.getPublishedArticles());
  }
}
