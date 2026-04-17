package com.app.fitness.mapper;

import com.app.fitness.dto.CompletedWorkoutRequest;
import com.app.fitness.dto.CompletedWorkoutResponse;
import com.fitness.workoutservice.model.CompletedWorkout;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompletedWorkoutMapper {

    CompletedWorkoutResponse toResponse(CompletedWorkout completedWorkout);

    @Mapping(target = "id", ignore = true)
    CompletedWorkout toEntity(CompletedWorkoutRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(CompletedWorkoutRequest request, @MappingTarget CompletedWorkout completedWorkout);
}
