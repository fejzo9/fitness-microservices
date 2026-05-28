package com.app.fitness.mapper;

import com.app.fitness.dto.UserRequest;
import com.app.fitness.dto.UserResponse;
import com.fitness.authservice.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.name", target = "roleName")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserRequest request, @MappingTarget User user);
}
