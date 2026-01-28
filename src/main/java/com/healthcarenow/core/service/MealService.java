package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.MealLogRequest;
import com.healthcarenow.core.dto.MealMacroDTO;
import com.healthcarenow.core.model.mongo.FoodDatabase;
import com.healthcarenow.core.model.mongo.Meal;
import com.healthcarenow.core.repository.mongo.FoodDatabaseRepository;
import com.healthcarenow.core.repository.mongo.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealService {
  private final MealRepository mealRepository;
  private final FoodDatabaseRepository foodDatabaseRepository;

  public List<FoodDatabase> searchFood(String query) {
    return foodDatabaseRepository.findByNameContainingIgnoreCase(query);
  }

  public Meal logMeal(String userId, MealLogRequest request) {
    Meal meal = new Meal();
    meal.setUserId(userId);
    meal.setMealType(request.getMealType());
    meal.setPhotoUrl(request.getPhotoUrl());
    meal.setTimestamp(LocalDateTime.now());
    meal.setFoodItems(request.getFoodItems());

    // Calculate totals
    double cals = 0, pro = 0, fat = 0, carbs = 0;
    if (request.getFoodItems() != null) {
      for (Meal.FoodItem item : request.getFoodItems()) {
        cals += item.getCalories();
        pro += item.getProteinG();
        fat += item.getFatG();
        carbs += item.getCarbsG();
      }
    }
    Meal.Nutrition nutrition = new Meal.Nutrition();
    nutrition.setCalories(cals);
    nutrition.setProteinG(pro);
    nutrition.setFatG(fat);
    nutrition.setCarbsG(carbs);
    meal.setTotalNutrition(nutrition);

    return mealRepository.save(meal);
  }

  public MealMacroDTO getDailyMacros(String userId) {
    LocalDateTime start = LocalDate.now().atStartOfDay();
    LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
    List<Meal> todayMeals = mealRepository.findByUserIdAndTimestampBetween(userId, start, end);

    double cals = 0, pro = 0, fat = 0, carbs = 0;
    for (Meal m : todayMeals) {
      if (m.getTotalNutrition() != null) {
        cals += m.getTotalNutrition().getCalories();
        pro += m.getTotalNutrition().getProteinG();
        fat += m.getTotalNutrition().getFatG();
        carbs += m.getTotalNutrition().getCarbsG();
      }
    }

    return MealMacroDTO.builder()
        .totalCalories(cals)
        .proteinG(pro)
        .fatG(fat)
        .carbsG(carbs)
        .build();
  }
}
