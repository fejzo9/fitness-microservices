package com.app.fitness.mapper;

import com.app.fitness.dto.WorkoutExerciseRequest;
import com.app.fitness.dto.WorkoutExerciseResponse;
import com.fitness.workoutservice.model.WorkoutExercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkoutExerciseMapper {

    @Mapping(source = "workoutDay.id", target = "workoutDayId")
    @Mapping(source = "exercise.id", target = "exerciseId")
    @Mapping(source = "exercise.name", target = "exerciseName")
    WorkoutExerciseResponse toResponse(WorkoutExercise workoutExercise);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workoutDay", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    WorkoutExercise toEntity(WorkoutExerciseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workoutDay", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    void updateEntity(WorkoutExerciseRequest request, @MappingTarget WorkoutExercise workoutExercise);
}
