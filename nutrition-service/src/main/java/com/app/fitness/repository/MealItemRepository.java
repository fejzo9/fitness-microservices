package com.app.fitness.repository;

import com.fitness.nutritionservice.model.MealItem;
import com.fitness.nutritionservice.model.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    boolean existsByMealLogAndFoodName(MealLog mealLog, String foodName);
}
