package com.app.fitness.mapper;

import com.app.fitness.dto.CompletedExerciseRequest;
import com.app.fitness.dto.CompletedExerciseResponse;
import com.fitness.workoutservice.model.CompletedExercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompletedExerciseMapper {

    @Mapping(source = "completedWorkout.id", target = "completedWorkoutId")
    @Mapping(source = "exercise.id", target = "exerciseId")
    @Mapping(source = "exercise.name", target = "exerciseName")
    CompletedExerciseResponse toResponse(CompletedExercise completedExercise);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "completedWorkout", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    CompletedExercise toEntity(CompletedExerciseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "completedWorkout", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    void updateEntity(CompletedExerciseRequest request, @MappingTarget CompletedExercise completedExercise);
}
