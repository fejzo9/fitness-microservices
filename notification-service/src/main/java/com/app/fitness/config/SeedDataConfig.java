package com.app.fitness.config;

import com.app.fitness.repository.NotificationRepository;
import com.fitness.notificationservice.model.Notification;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(NotificationRepository notificationRepository) {

        return args -> {
            createNotificationIfMissing(notificationRepository, 3L, "Welcome to the platform!", "INFO", false);
            createNotificationIfMissing(
                    notificationRepository,
                    3L,
                    "Your trainer assigned a new program.",
                    "ALERT",
                    false);
            createNotificationIfMissing(notificationRepository, 4L, "Track your meals daily.", "REMINDER", false);
        };
    }

    private void createNotificationIfMissing(
            NotificationRepository notificationRepository,
            Long userId,
            String message,
            String type,
            boolean isRead) {

        if (!notificationRepository.existsByUserIdAndMessageAndType(userId, message, type)) {
            notificationRepository.save(Notification.builder()
                    .userId(userId)
                    .message(message)
                    .type(type)
                    .isRead(isRead)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
