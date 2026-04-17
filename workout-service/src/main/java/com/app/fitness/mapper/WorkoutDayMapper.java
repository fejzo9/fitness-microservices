package com.app.fitness.mapper;

import com.app.fitness.dto.WorkoutDayRequest;
import com.app.fitness.dto.WorkoutDayResponse;
import com.fitness.workoutservice.model.WorkoutDay;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkoutDayMapper {

    @Mapping(source = "workoutPlan.id", target = "workoutPlanId")
    WorkoutDayResponse toResponse(WorkoutDay workoutDay);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workoutPlan", ignore = true)
    WorkoutDay toEntity(WorkoutDayRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workoutPlan", ignore = true)
    void updateEntity(WorkoutDayRequest request, @MappingTarget WorkoutDay workoutDay);
}
