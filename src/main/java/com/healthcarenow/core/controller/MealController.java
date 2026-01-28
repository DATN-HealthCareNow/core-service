package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.MealLogRequest;
import com.healthcarenow.core.dto.MealMacroDTO;
import com.healthcarenow.core.model.mongo.FoodDatabase;
import com.healthcarenow.core.model.mongo.Meal;
import com.healthcarenow.core.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MealController {
  private final MealService mealService;

  @GetMapping("/food/search")
  public ResponseEntity<List<FoodDatabase>> searchFood(@RequestParam String query) {
    return ResponseEntity.ok(mealService.searchFood(query));
  }

  @PostMapping("/meals/log")
  public ResponseEntity<Meal> logMeal(@RequestHeader("X-User-Id") String userId, @RequestBody MealLogRequest request) {
    return ResponseEntity.ok(mealService.logMeal(userId, request));
  }

  @GetMapping("/meals/macros")
  public ResponseEntity<MealMacroDTO> getDailyMacros(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(mealService.getDailyMacros(userId));
  }
}
