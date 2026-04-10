package com.app.fitness.repository;

import com.fitness.userservice.model.Notification;
import com.fitness.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByUserAndMessageAndType(User user, String message, String type);
}
