package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.admin.ArticleResponse;
import com.healthcarenow.core.dto.admin.ArticleUpsertRequest;
import com.healthcarenow.core.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;

  @GetMapping
  public ResponseEntity<List<ArticleResponse>> getPublishedArticles() {
    return ResponseEntity.ok(articleService.getAdminArticles());
  }

  @PostMapping
  public ResponseEntity<ArticleResponse> createArticle(
      @RequestBody ArticleUpsertRequest request
  ) {
    return ResponseEntity.ok(articleService.createArticle(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ArticleResponse> updateArticle(
      @PathVariable String id,
      @RequestBody ArticleUpsertRequest request
  ) {
    return ResponseEntity.ok(articleService.updateArticle(id, request));
  }

  @PutMapping("/{id}/publish")
  public ResponseEntity<ArticleResponse> publishArticle(
      @PathVariable String id
  ) {
    return ResponseEntity.ok(articleService.publishArticle(id));
  }


}
