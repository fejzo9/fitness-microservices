package com.app.fitness.mapper;

import com.app.fitness.dto.MealEntryRequest;
import com.app.fitness.dto.MealEntryResponse;
import com.fitness.nutritionservice.model.MealEntry;
import org.springframework.stereotype.Component;

@Component
public class MealEntryMapper {

    public MealEntry toEntity(MealEntryRequest request) {
        return MealEntry.builder()
                .userId(request.getUserId())
                .entryDate(request.getEntryDate())
                .mealTime(request.getMealTime())
                .mealName(request.getMealName())
                .calories(request.getCalories())
                .proteinG(request.getProteinG())
                .carbsG(request.getCarbsG())
                .fatsG(request.getFatsG())
                .build();
    }

    public MealEntryResponse toResponse(MealEntry entity) {
        return MealEntryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .entryDate(entity.getEntryDate())
                .mealTime(entity.getMealTime())
                .mealName(entity.getMealName())
                .calories(entity.getCalories())
                .proteinG(entity.getProteinG())
                .carbsG(entity.getCarbsG())
                .fatsG(entity.getFatsG())
                .build();
    }

    public void updateEntity(MealEntry entity, MealEntryRequest request) {
        entity.setUserId(request.getUserId());
        entity.setEntryDate(request.getEntryDate());
        entity.setMealTime(request.getMealTime());
        entity.setMealName(request.getMealName());
        entity.setCalories(request.getCalories());
        entity.setProteinG(request.getProteinG());
        entity.setCarbsG(request.getCarbsG());
        entity.setFatsG(request.getFatsG());
    }
}
