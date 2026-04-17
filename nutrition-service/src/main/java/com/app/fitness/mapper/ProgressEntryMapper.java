package com.app.fitness.mapper;

import com.app.fitness.dto.ProgressEntryRequest;
import com.app.fitness.dto.ProgressEntryResponse;
import com.fitness.nutritionservice.model.ProgressEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProgressEntryMapper {

    ProgressEntryResponse toResponse(ProgressEntry progressEntry);

    @Mapping(target = "id", ignore = true)
    ProgressEntry toEntity(ProgressEntryRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ProgressEntryRequest request, @MappingTarget ProgressEntry progressEntry);
}
