package com.app.fitness.mapper;

import com.app.fitness.dto.FitnessGoalRequest;
import com.app.fitness.dto.FitnessGoalResponse;
import com.fitness.userservice.model.FitnessGoal;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FitnessGoalMapper {

    FitnessGoalResponse toResponse(FitnessGoal goal);

    @Mapping(target = "id", ignore = true)
    FitnessGoal toEntity(FitnessGoalRequest request);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(FitnessGoalRequest request, @MappingTarget FitnessGoal goal);
}
