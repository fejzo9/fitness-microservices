package com.app.fitness.repository;

import com.fitness.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByUserIdAndMessageAndType(Long userId, String message, String type);
}
