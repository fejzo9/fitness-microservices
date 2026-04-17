package com.app.fitness.mapper;

import com.app.fitness.dto.MealLogRequest;
import com.app.fitness.dto.MealLogResponse;
import com.fitness.nutritionservice.model.MealLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MealLogMapper {

    MealLogResponse toResponse(MealLog mealLog);

    @Mapping(target = "id", ignore = true)
    MealLog toEntity(MealLogRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(MealLogRequest request, @MappingTarget MealLog mealLog);
}
