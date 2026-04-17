package com.app.fitness.mapper;

import com.app.fitness.dto.ExerciseRequest;
import com.app.fitness.dto.ExerciseResponse;
import com.fitness.workoutservice.model.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    ExerciseResponse toResponse(Exercise exercise);

    @Mapping(target = "id", ignore = true)
    Exercise toEntity(ExerciseRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ExerciseRequest request, @MappingTarget Exercise exercise);
}
