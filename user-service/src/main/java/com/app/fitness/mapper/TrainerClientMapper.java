package com.app.fitness.mapper;

import com.app.fitness.dto.TrainerClientRequest;
import com.app.fitness.dto.TrainerClientResponse;
import com.fitness.userservice.model.TrainerClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TrainerClientMapper {

    TrainerClientResponse toResponse(TrainerClient trainerClient);

    @Mapping(target = "id", ignore = true)
    TrainerClient toEntity(TrainerClientRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(TrainerClientRequest request, @MappingTarget TrainerClient trainerClient);
}
