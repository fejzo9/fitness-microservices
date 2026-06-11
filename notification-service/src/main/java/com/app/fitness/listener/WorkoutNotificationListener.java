package com.app.fitness.listener;

import com.app.fitness.config.RabbitMQConfig;
import com.app.fitness.dto.WorkoutNotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkoutNotificationListener {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleWorkoutNotification(WorkoutNotificationMessage message) {
        log.info("RECEIVED WORKOUT NOTIFICATION FROM RABBITMQ:");
        log.info("User ID: {}", message.getUserId());
        log.info("Date: {}", message.getDate());
        log.info("Completed Exercises: {}", message.getCompletedExercises());
        log.info("Uncompleted Exercises: {}", message.getUncompletedExercises());
        log.info("--------------------------------------------------");
    }
}
