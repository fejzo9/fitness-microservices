package com.app.fitness.service;

import com.app.fitness.config.RabbitMQConfig;
import com.app.fitness.dto.WorkoutNotificationMessage;
import com.app.fitness.model.NotificationLog;
import com.app.fitness.model.WorkoutExercise;
import com.app.fitness.repository.NotificationLogRepository;
import com.app.fitness.repository.WorkoutExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutNotificationService {

    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final RabbitTemplate rabbitTemplate;

    // Cron job: Every day at 8:00 AM (for example, or any time)
    // Here we use cron for daily trigger
    @Scheduled(cron = "0 0 8 * * *")
    public void scheduleDailyNotifications() {
        log.info("Starting scheduled daily workout notifications...");
        processNotificationsForDate(LocalDate.now());
    }

    @Transactional
    public void processNotificationsForDate(LocalDate date) {
        log.info("Processing notifications for date: {}", date);
        
        // Find all exercises for the given date
        List<WorkoutExercise> exercises = workoutExerciseRepository.findByScheduledDate(date);
        
        if (exercises.isEmpty()) {
            log.info("No exercises found for date: {}", date);
            return;
        }

        // Group exercises by userId
        Map<Long, List<WorkoutExercise>> userExercisesMap = exercises.stream()
                .collect(Collectors.groupingBy(WorkoutExercise::getUserId));

        for (Map.Entry<Long, List<WorkoutExercise>> entry : userExercisesMap.entrySet()) {
            Long userId = entry.getKey();
            List<WorkoutExercise> userExercises = entry.getValue();

            // Check if notification already sent for this user and date
            boolean alreadySent = notificationLogRepository.findByUserIdAndNotificationDate(userId, date)
                    .map(NotificationLog::getNotificationSent)
                    .orElse(false);

            if (!alreadySent) {
                sendNotification(userId, userExercises, date);
            } else {
                log.info("Notification already sent for user {} on date {}", userId, date);
            }
        }
    }

    private void sendNotification(Long userId, List<WorkoutExercise> exercises, LocalDate date) {
        int completed = (int) exercises.stream().filter(ex -> Boolean.TRUE.equals(ex.getCompleted())).count();
        int uncompleted = exercises.size() - completed;

        WorkoutNotificationMessage message = WorkoutNotificationMessage.builder()
                .userId(userId)
                .completedExercises(completed)
                .uncompletedExercises(uncompleted)
                .date(date.toString())
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    message
            );

            // Log that notification was sent
            NotificationLog logEntry = notificationLogRepository.findByUserIdAndNotificationDate(userId, date)
                    .orElse(NotificationLog.builder()
                            .userId(userId)
                            .notificationDate(date)
                            .build());
            
            logEntry.setNotificationSent(true);
            String formattedMessage = String.format("Email podsjetnik za broj vježbi: %d na dan %s je uspješno poslan.", exercises.size(), date);
            logEntry.setMessage(formattedMessage);
            notificationLogRepository.save(logEntry);

            log.info("Notification sent to RabbitMQ for user {}: {}", userId, message);
        } catch (Exception e) {
            log.error("Failed to send notification for user {}: {}", userId, e.getMessage());
        }
    }

    @Transactional
    public void resetNotificationFlags() {
        log.info("Resetting all notification_sent flags to false");
        List<NotificationLog> logs = notificationLogRepository.findAll();
        logs.forEach(l -> l.setNotificationSent(false));
        notificationLogRepository.saveAll(logs);
    }

    public List<NotificationLog> getLatestNotifications(Long userId) {
        return notificationLogRepository.findTop2ByUserIdAndNotificationSentTrueOrderByNotificationDateDesc(userId);
    }
}
