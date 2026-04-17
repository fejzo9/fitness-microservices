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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void findAll_shouldReturnMappedList() {
        Notification notification = Notification.builder().id(1L).userId(3L).message("Welcome!").type("INFO")
                .isRead(false).createdAt(LocalDateTime.now()).build();
        NotificationResponse response = new NotificationResponse(1L, 3L, "Welcome!", "INFO", false,
                LocalDateTime.now());
        when(notificationRepository.findAll()).thenReturn(List.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        List<NotificationResponse> result = notificationService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("INFO");
    }

    @Test
    void findById_whenNotFound_shouldThrow() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenDuplicate_shouldThrow() {
        NotificationRequest request = new NotificationRequest(3L, "Welcome!", "INFO", false);
        when(notificationRepository.existsByUserIdAndMessageAndType(3L, "Welcome!", "INFO")).thenReturn(true);

        assertThatThrownBy(() -> notificationService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_withValidRequest_shouldSaveAndReturn() {
        NotificationRequest request = new NotificationRequest(3L, "New message", "ALERT", false);
        Notification entity = Notification.builder().userId(3L).message("New message").type("ALERT").isRead(false)
                .build();
        Notification saved = Notification.builder().id(5L).userId(3L).message("New message").type("ALERT")
                .isRead(false).createdAt(LocalDateTime.now()).build();
        NotificationResponse response = new NotificationResponse(5L, 3L, "New message", "ALERT", false,
                LocalDateTime.now());

        when(notificationRepository.existsByUserIdAndMessageAndType(any(), any(), any())).thenReturn(false);
        when(notificationMapper.toEntity(request)).thenReturn(entity);
        when(notificationRepository.save(entity)).thenReturn(saved);
        when(notificationMapper.toResponse(saved)).thenReturn(response);

        NotificationResponse result = notificationService.create(request);

        assertThat(result.getId()).isEqualTo(5L);
        verify(notificationRepository).save(entity);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(notificationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldDeleteById() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        notificationService.delete(1L);

        verify(notificationRepository).deleteById(1L);
    }
}
