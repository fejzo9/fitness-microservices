package com.app.fitness.repository;

import com.fitness.nutritionservice.model.MealItem;
import com.fitness.nutritionservice.model.MealLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    boolean existsByMealLogAndFoodName(MealLog mealLog, String foodName);

    @Override
    @EntityGraph(attributePaths = "mealLog")
    List<MealItem> findAll();

    @Override
    @EntityGraph(attributePaths = "mealLog")
    Optional<MealItem> findById(Long id);
}
