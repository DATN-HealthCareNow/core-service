package com.healthcarenow.core.dto;

import com.healthcarenow.core.model.mongo.Meal;
import lombok.Data;
import java.util.List;

@Data
public class MealLogRequest {
  private String mealType;
  private String photoUrl;
  private List<Meal.FoodItem> foodItems;
}
