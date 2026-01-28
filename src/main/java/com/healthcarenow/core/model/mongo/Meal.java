package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "meals")
@Data
public class Meal {
  @Id
  private String id;

  @Indexed
  private String userId;

  private LocalDateTime timestamp;
  private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK
  private String photoUrl;
  private String aiAnalysisId;

  private List<FoodItem> foodItems;
  private Nutrition totalNutrition;

  @Data
  public static class FoodItem {
    private String name;
    private Integer quantityG;
    private Double calories;
    private Double proteinG;
    private Double fatG;
    private Double carbsG;
    private Double fiberG;
    private Double aiConfidence;
  }

  @Data
  public static class Nutrition {
    private Double calories;
    private Double proteinG;
    private Double fatG;
    private Double carbsG;
    private Double fiberG;
  }
}
