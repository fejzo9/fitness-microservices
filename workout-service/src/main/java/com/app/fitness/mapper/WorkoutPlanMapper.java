package com.app.fitness.mapper;

import com.app.fitness.dto.WorkoutPlanRequest;
import com.app.fitness.dto.WorkoutPlanResponse;
import com.fitness.workoutservice.model.WorkoutPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkoutPlanMapper {

    WorkoutPlanResponse toResponse(WorkoutPlan workoutPlan);

    @Mapping(target = "id", ignore = true)
    WorkoutPlan toEntity(WorkoutPlanRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(WorkoutPlanRequest request, @MappingTarget WorkoutPlan workoutPlan);
}
