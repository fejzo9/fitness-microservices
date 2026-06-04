package com.app.fitness.mapper;

import com.app.fitness.dto.ExerciseCategoryRequest;
import com.app.fitness.dto.ExerciseCategoryResponse;
import com.app.fitness.model.ExerciseCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExerciseCategoryMapper {

    ExerciseCategoryResponse toResponse(ExerciseCategory category);

    @Mapping(target = "id", ignore = true)
    ExerciseCategory toEntity(ExerciseCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ExerciseCategoryRequest request, @MappingTarget ExerciseCategory category);
}
