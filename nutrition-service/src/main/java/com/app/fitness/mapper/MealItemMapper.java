package com.app.fitness.mapper;

import com.app.fitness.dto.MealItemRequest;
import com.app.fitness.dto.MealItemResponse;
import com.fitness.nutritionservice.model.MealItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MealItemMapper {

    @Mapping(source = "mealLog.id", target = "mealLogId")
    MealItemResponse toResponse(MealItem mealItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mealLog", ignore = true)
    MealItem toEntity(MealItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mealLog", ignore = true)
    void updateEntity(MealItemRequest request, @MappingTarget MealItem mealItem);
}
