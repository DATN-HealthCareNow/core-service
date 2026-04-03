package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.admin.ArticleAIGenerateRequest;
import com.healthcarenow.core.dto.admin.ArticleAIGenerateResponse;
import com.healthcarenow.core.dto.admin.ArticleResponse;
import com.healthcarenow.core.dto.admin.ArticleUpsertRequest;
import com.healthcarenow.core.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/dashboard/articles")
@RequiredArgsConstructor
public class InternalArticleController {

  private final ArticleService articleService;

  @GetMapping
  public ResponseEntity<List<ArticleResponse>> getAdminArticles() {
    return ResponseEntity.ok(articleService.getAdminArticles());
  }

  @PostMapping
  public ResponseEntity<ArticleResponse> createArticle(@Valid @RequestBody ArticleUpsertRequest request) {
    return ResponseEntity.ok(articleService.createArticle(request));
  }

  @PutMapping("/{articleId}")
  public ResponseEntity<ArticleResponse> updateArticle(
      @PathVariable String articleId,
      @Valid @RequestBody ArticleUpsertRequest request) {
    return ResponseEntity.ok(articleService.updateArticle(articleId, request));
  }

  @PatchMapping("/{articleId}/publish")
  public ResponseEntity<ArticleResponse> publishArticle(@PathVariable String articleId) {
    return ResponseEntity.ok(articleService.publishArticle(articleId));
  }

  @PostMapping("/generate-draft")
  public ResponseEntity<ArticleAIGenerateResponse> generateDraft(@RequestBody ArticleAIGenerateRequest request) {
    return ResponseEntity.ok(articleService.generateDraft(request));
  }
}
