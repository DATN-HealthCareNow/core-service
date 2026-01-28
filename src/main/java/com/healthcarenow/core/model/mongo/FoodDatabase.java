package com.healthcarenow.core.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "food_database")
@Data
public class FoodDatabase {
  @Id
  private String id;

  @TextIndexed
  private String name;

  private String brand;
  private Integer servingSizeG;
  private boolean verified;
  private String source;

  private NutritionPer100g nutritionPer100g;

  @Data
  public static class NutritionPer100g {
    private Double calories;
    private Double proteinG;
    private Double fatG;
    private Double carbsG;
  }
}
