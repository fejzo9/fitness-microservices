package com.app.fitness.mapper;

import com.app.fitness.dto.ExerciseCategoryMapRequest;
import com.app.fitness.dto.ExerciseCategoryMapResponse;
import com.fitness.workoutservice.model.ExerciseCategoryMap;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExerciseCategoryMapMapper {

    @Mapping(source = "exercise.id", target = "exerciseId")
    @Mapping(source = "exercise.name", target = "exerciseName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ExerciseCategoryMapResponse toResponse(ExerciseCategoryMap map);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "category", ignore = true)
    ExerciseCategoryMap toEntity(ExerciseCategoryMapRequest request);
}
