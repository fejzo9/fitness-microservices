package com.app.fitness.listener;

import com.app.fitness.config.RabbitMQConfig;
import com.app.fitness.event.UserDeletionEvent;
import com.app.fitness.event.UserDeletionResponseEvent;
import com.app.fitness.repository.FitnessGoalRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletionListener {

    private final FitnessGoalRepository fitnessGoalRepository;
    private final RabbitTemplate rabbitTemplate;
    private final EntityManager entityManager;

    @RabbitListener(queues = RabbitMQConfig.DELETION_QUEUE)
    @Transactional
    public void handleDeletionEvent(UserDeletionEvent event) {
        log.info("Received UserDeletionEvent: userId={}, type={}", event.getUserId(), event.getType());

        try {
            if (event.getType() == UserDeletionEvent.Type.START) {
                // Lokalna transakcija: Brisanje fitness ciljeva korisnika
                fitnessGoalRepository.deleteByUserId(event.getUserId());
                
                // Osiguraj da se SQL izvrši unutar try-catch bloka
                entityManager.flush();
                
                sendResponse(event.getUserId(), UserDeletionResponseEvent.Status.SUCCESS);
            } else if (event.getType() == UserDeletionEvent.Type.ROLLBACK) {
                // Inverzna akcija
                log.info("Rolling back deletion for userId={} in user-service", event.getUserId());
                // U stvarnom scenariju bismo vratili podatke iz backupa ili oznacili status kao ACTIVE
            }
        } catch (Exception e) {
            log.error("Error processing deletion for userId={}", event.getUserId(), e);
            sendResponse(event.getUserId(), UserDeletionResponseEvent.Status.FAILURE);
        }
    }

    private void sendResponse(Long userId, UserDeletionResponseEvent.Status status) {
        UserDeletionResponseEvent response = UserDeletionResponseEvent.builder()
                .userId(userId)
                .service("user-service")
                .status(status)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.RESPONSE_EXCHANGE, "user.deletion.response", response);
    }
}
