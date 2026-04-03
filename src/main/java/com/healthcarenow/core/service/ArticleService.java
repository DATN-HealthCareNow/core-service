package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.NotificationEvent;
import com.healthcarenow.core.dto.admin.ArticleAIGenerateRequest;
import com.healthcarenow.core.dto.admin.ArticleAIGenerateResponse;
import com.healthcarenow.core.dto.admin.ArticleResponse;
import com.healthcarenow.core.dto.admin.ArticleUpsertRequest;
import com.healthcarenow.core.exception.ResourceNotFoundException;
import com.healthcarenow.core.model.mongo.Article;
import com.healthcarenow.core.model.mongo.User;
import com.healthcarenow.core.repository.mongo.ArticleRepository;
import com.healthcarenow.core.repository.mongo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArticleService {

  private static final String STATUS_DRAFT = "DRAFT";
  private static final String STATUS_PUBLISHED = "PUBLISHED";

  private final ArticleRepository articleRepository;
  private final UserRepository userRepository;
  private final NotificationProducer notificationProducer;

  public List<ArticleResponse> getAdminArticles() {
    return articleRepository.findAllByOrderByUpdatedAtDesc().stream()
        .map(this::toResponse)
        .toList();
  }

  public ArticleResponse createArticle(ArticleUpsertRequest request) {
    Article article = new Article();
    applyUpsert(article, request);
    LocalDateTime now = LocalDateTime.now();
    article.setCreatedAt(now);
    article.setUpdatedAt(now);
    article.setViews(0L);

    if (!StringUtils.hasText(article.getStatus())) {
      article.setStatus(STATUS_DRAFT);
    }

    if (STATUS_PUBLISHED.equalsIgnoreCase(article.getStatus())) {
      article.setPublishedAt(now);
    }

    Article saved = articleRepository.save(article);

    if (STATUS_PUBLISHED.equalsIgnoreCase(saved.getStatus())) {
      fanOutNewArticleNotification(saved);
    }

    return toResponse(saved);
  }

  public ArticleResponse updateArticle(String articleId, ArticleUpsertRequest request) {
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

    String previousStatus = article.getStatus();
    applyUpsert(article, request);
    article.setUpdatedAt(LocalDateTime.now());

    if (STATUS_PUBLISHED.equalsIgnoreCase(article.getStatus()) && article.getPublishedAt() == null) {
      article.setPublishedAt(LocalDateTime.now());
    }

    Article saved = articleRepository.save(article);

    if (!STATUS_PUBLISHED.equalsIgnoreCase(previousStatus)
        && STATUS_PUBLISHED.equalsIgnoreCase(saved.getStatus())) {
      fanOutNewArticleNotification(saved);
    }

    return toResponse(saved);
  }

  public ArticleResponse publishArticle(String articleId) {
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

    article.setStatus(STATUS_PUBLISHED);
    article.setPublishedAt(LocalDateTime.now());
    article.setUpdatedAt(LocalDateTime.now());

    Article saved = articleRepository.save(article);
    fanOutNewArticleNotification(saved);
    return toResponse(saved);
  }

  public List<ArticleResponse> getPublishedArticles() {
    return articleRepository.findByStatusOrderByPublishedAtDesc(STATUS_PUBLISHED).stream()
        .sorted(Comparator.comparing(Article::getPublishedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
        .map(this::toResponse)
        .toList();
  }

  public ArticleAIGenerateResponse generateDraft(ArticleAIGenerateRequest request) {
    String topic = StringUtils.hasText(request.getTopic()) ? request.getTopic().trim() : "Healthy living";
    String category = StringUtils.hasText(request.getCategory()) ? request.getCategory().trim() : "General";

    List<String> keywords = request.getSeoKeywords() != null ? request.getSeoKeywords() : List.of();
    List<String> normalizedKeywords = new ArrayList<>(keywords.stream()
        .filter(StringUtils::hasText)
        .map(String::trim)
        .toList());

    String title = "Guide: " + topic + " for Everyday Health";
    String summary = "A practical " + category.toLowerCase(Locale.ROOT)
        + " guide with actionable steps, early warning signs, and professional-care recommendations.";

    String primaryKeyword = normalizedKeywords.isEmpty() ? topic : normalizedKeywords.getFirst();

    String content = "## Why " + topic + " Matters\n"
        + "Maintaining long-term health starts with understanding risk factors and daily habits.\n\n"
        + "## Key Daily Actions\n"
        + "- Follow a balanced meal plan rich in fiber and lean protein.\n"
        + "- Track core health signals weekly (heart rate, sleep, hydration).\n"
        + "- Keep at least 150 minutes of moderate exercise each week.\n\n"
        + "## When to Seek Medical Advice\n"
        + "If symptoms persist or worsen, consult a qualified healthcare professional promptly.\n\n"
        + "## FAQ\n"
        + "### Is this enough without doctor consultation?\n"
        + "No. This article provides educational support and does not replace medical diagnosis.";

    return ArticleAIGenerateResponse.builder()
        .title(title)
        .summary(summary)
        .content(content)
        .seoKeywords(normalizedKeywords)
        .metaTitle(title + " | HealthCareNow")
        .metaDescription("Learn " + topic + " with evidence-based tips and clear next steps for safer outcomes.")
        .slugSuggestion(toSlug(primaryKeyword + " " + category))
        .build();
  }

  private void fanOutNewArticleNotification(Article article) {
    String articleTitle = article.getTitle();
    String articleId = article.getId();

    userRepository.findAll().stream()
        .filter(user -> StringUtils.hasText(user.getDeviceToken()))
        .forEach(user -> {
          NotificationEvent event = NotificationEvent.builder()
              .eventType("NEW_ARTICLE_PUBLISHED")
              .priority("NORMAL")
              .userId(user.getId())
              .payload(Map.of(
                  "language", "vi",
                  "article_title", articleTitle,
                  "article_id", articleId,
                  "article_category", article.getCategory() != null ? article.getCategory() : "General"))
              .build();
          notificationProducer.sendNotification(event);
        });
  }

  private void applyUpsert(Article article, ArticleUpsertRequest request) {
    article.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
    article.setSlug(toSlug(request.getTitle()));
    article.setCategory(request.getCategory());
    article.setSummary(request.getSummary());
    article.setContent(request.getContent());
    article.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim().toUpperCase(Locale.ROOT) : STATUS_DRAFT);
    article.setSeoKeywords(request.getSeoKeywords() != null ? request.getSeoKeywords() : List.of());
    article.setMetaTitle(request.getMetaTitle());
    article.setMetaDescription(request.getMetaDescription());
    article.setCoverImageUrl(request.getCoverImageUrl());
    article.setAiGenerated(Boolean.TRUE.equals(request.getAiGenerated()));
    article.setAuthorId(StringUtils.hasText(request.getAuthorId()) ? request.getAuthorId() : "system");
    article.setAuthorName(StringUtils.hasText(request.getAuthorName()) ? request.getAuthorName() : "HealthCareNow AI Studio");
    article.setScheduledAt(request.getScheduledAt());
  }

  private ArticleResponse toResponse(Article article) {
    return ArticleResponse.builder()
        .id(article.getId())
        .title(article.getTitle())
        .slug(article.getSlug())
        .category(article.getCategory())
        .summary(article.getSummary())
        .content(article.getContent())
        .status(article.getStatus())
        .seoKeywords(article.getSeoKeywords())
        .metaTitle(article.getMetaTitle())
        .metaDescription(article.getMetaDescription())
        .coverImageUrl(article.getCoverImageUrl())
        .aiGenerated(article.isAiGenerated())
        .authorName(article.getAuthorName())
        .views(article.getViews() != null ? article.getViews() : 0)
        .scheduledAt(article.getScheduledAt())
        .publishedAt(article.getPublishedAt())
        .updatedAt(article.getUpdatedAt())
        .build();
  }

  private String toSlug(String input) {
    if (!StringUtils.hasText(input)) {
      return "article-" + System.currentTimeMillis();
    }

    String normalized = input.toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9\\s-]", "")
        .trim()
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-");

    return StringUtils.hasText(normalized) ? normalized : "article-" + System.currentTimeMillis();
  }
}
