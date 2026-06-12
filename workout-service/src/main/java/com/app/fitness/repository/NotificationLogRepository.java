package com.app.fitness.repository;

import com.app.fitness.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Optional<NotificationLog> findByUserIdAndNotificationDate(Long userId, LocalDate date);
    List<NotificationLog> findTop2ByUserIdAndNotificationSentTrueOrderByNotificationDateDesc(Long userId);
}
