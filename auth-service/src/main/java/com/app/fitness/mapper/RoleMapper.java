package com.app.fitness.mapper;

import com.app.fitness.dto.RoleRequest;
import com.app.fitness.dto.RoleResponse;
import com.fitness.authservice.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);

    @Mapping(target = "id", ignore = true)
    Role toEntity(RoleRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(RoleRequest request, @MappingTarget Role role);
}
