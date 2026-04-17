package com.app.fitness.service;

import com.app.fitness.dto.NotificationRequest;
import com.app.fitness.dto.NotificationResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.NotificationMapper;
import com.app.fitness.repository.NotificationRepository;
import com.fitness.notificationservice.model.Notification;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll().stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse findById(Long id) {
        return notificationRepository.findById(id)
                .map(notificationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        if (notificationRepository.existsByUserIdAndMessageAndType(
                request.getUserId(), request.getMessage(), request.getType())) {
            throw new DuplicateResourceException(
                    "Notification already exists for userId=" + request.getUserId()
                            + ", type=" + request.getType());
        }
        Notification notification = notificationMapper.toEntity(request);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse update(Long id, NotificationRequest request) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notificationMapper.updateEntity(request, notification);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void delete(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }
}
