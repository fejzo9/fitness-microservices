package com.app.fitness.repository;

import com.fitness.nutritionservice.model.MealLog;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    boolean existsByUserIdAndLogDateAndMealType(Long userId, LocalDate logDate, String mealType);

    Optional<MealLog> findByUserIdAndLogDateAndMealType(Long userId, LocalDate logDate, String mealType);

    void deleteByUserId(Long userId);
}
