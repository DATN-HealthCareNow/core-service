package com.healthcarenow.core.repository.mongo;

import com.healthcarenow.core.model.mongo.Article;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

  List<Article> findAllByOrderByUpdatedAtDesc();

  List<Article> findByStatusOrderByPublishedAtDesc(String status);

  Optional<Article> findBySlug(String slug);
}
