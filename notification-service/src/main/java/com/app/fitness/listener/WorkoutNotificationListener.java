package com.app.fitness.listener;

import com.app.fitness.client.AuthClient;
import com.app.fitness.config.RabbitMQConfig;
import com.app.fitness.dto.UserResponse;
import com.app.fitness.dto.WorkoutNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkoutNotificationListener {

    private final AuthClient authClient;
    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleWorkoutNotification(WorkoutNotificationMessage message) {
        log.info("RECEIVED WORKOUT NOTIFICATION FROM RABBITMQ FOR USER ID: {}", message.getUserId());
        
        try {
            // 1. Fetch user email from auth-service
            UserResponse user = authClient.getUserById(message.getUserId());
            String email = user.getEmail();
            log.info("Fetched email for user {}: {}", message.getUserId(), email);

            // 2. Send email
            sendEmail(email, message);
            
            log.info("Notification process completed for user: {}", message.getUserId());
        } catch (Exception e) {
            log.error("Failed to process notification for user {}: {}", message.getUserId(), e.getMessage());
        }
    }

    private void sendEmail(String to, WorkoutNotificationMessage message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("notifications@fitnessapp.com");
        mailMessage.setTo(to);
        mailMessage.setSubject("Daily Workout Progress - " + message.getDate());
        
        String body = String.format(
            "Hello,\n\nHere is your workout progress for %s:\n" +
            "- Completed exercises: %d\n" +
            "- Uncompleted exercises: %d\n\n" +
            "Keep up the good work!\n" +
            "Fitness App Team",
            message.getDate(),
            message.getCompletedExercises(),
            message.getUncompletedExercises()
        );
        
        mailMessage.setText(body);
        
        mailSender.send(mailMessage);
        log.info("Email sent successfully to {}", to);
    }
}
