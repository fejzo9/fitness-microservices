package com.app.fitness.repository;

import com.fitness.nutritionservice.model.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    List<MealEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    List<MealEntry> findByUserId(Long userId);

    @Query("SELECT m FROM MealEntry m WHERE m.userId = :userId AND m.entryDate BETWEEN :startDate AND :endDate ORDER BY m.entryDate, m.mealTime")
    List<MealEntry> findByUserIdAndDateRange(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
