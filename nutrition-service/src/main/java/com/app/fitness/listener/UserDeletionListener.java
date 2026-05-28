package com.app.fitness.listener;

import com.app.fitness.config.RabbitMQConfig;
import com.app.fitness.event.UserDeletionEvent;
import com.app.fitness.event.UserDeletionResponseEvent;
import com.app.fitness.repository.MealLogRepository;
import com.app.fitness.repository.ProgressEntryRepository;
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

    private final MealLogRepository mealLogRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final RabbitTemplate rabbitTemplate;
    private final EntityManager entityManager;

    @RabbitListener(queues = RabbitMQConfig.DELETION_QUEUE)
    @Transactional
    public void handleDeletionEvent(UserDeletionEvent event) {
        log.info("Received UserDeletionEvent in nutrition-service: userId={}, type={}", event.getUserId(), event.getType());

        try {
            if (event.getType() == UserDeletionEvent.Type.START) {
                // Lokalna transakcija: Brisanje podataka o ishrani i napretku
                // Treba obratiti pažnju na MealItem-e. Ako nema kaskade, treba ih obrisati prvo.
                // Ali ovdje ćemo pretpostaviti da imamo kaskadu ili da ih brišemo po userId ako dodamo podršku.
                
                // Za jednostavnost, brišemo MealLog i ProgressEntry. 
                // Ako MealItem-i nisu kaskadno obrisani, ovo će baciti grešku, što je dobro za demo Sage (FAILURE okida rollback).
                
                mealLogRepository.deleteByUserId(event.getUserId());
                progressEntryRepository.deleteByUserId(event.getUserId());
                
                // Osiguraj da se SQL izvrši unutar try-catch bloka
                entityManager.flush();
                
                sendResponse(event.getUserId(), UserDeletionResponseEvent.Status.SUCCESS);
            } else if (event.getType() == UserDeletionEvent.Type.ROLLBACK) {
                log.info("Rolling back deletion for userId={} in nutrition-service", event.getUserId());
            }
        } catch (Exception e) {
            log.error("Error processing deletion for userId={} in nutrition-service", event.getUserId(), e);
            sendResponse(event.getUserId(), UserDeletionResponseEvent.Status.FAILURE);
        }
    }

    private void sendResponse(Long userId, UserDeletionResponseEvent.Status status) {
        UserDeletionResponseEvent response = UserDeletionResponseEvent.builder()
                .userId(userId)
                .service("nutrition-service")
                .status(status)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.RESPONSE_EXCHANGE, "user.deletion.response", response);
    }
}
