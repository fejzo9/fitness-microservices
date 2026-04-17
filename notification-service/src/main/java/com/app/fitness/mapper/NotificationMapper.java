package com.app.fitness.mapper;

import com.app.fitness.dto.NotificationRequest;
import com.app.fitness.dto.NotificationResponse;
import com.fitness.notificationservice.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Notification toEntity(NotificationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(NotificationRequest request, @MappingTarget Notification notification);
}
