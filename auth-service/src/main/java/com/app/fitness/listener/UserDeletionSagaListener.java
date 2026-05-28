package com.app.fitness.listener;

import com.app.fitness.config.RabbitMQConfig;
import com.app.fitness.event.UserDeletionEvent;
import com.app.fitness.event.UserDeletionResponseEvent;
import com.app.fitness.repository.UserRepository;
import com.fitness.authservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletionSagaListener {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.RESPONSE_QUEUE)
    @Transactional
    public void handleResponse(UserDeletionResponseEvent response) {
        log.info("Received deletion response from {}: userId={}, status={}", 
                response.getService(), response.getUserId(), response.getStatus());

        userRepository.findById(response.getUserId()).ifPresent(user -> {
            if ("user-service".equals(response.getService())) {
                user.setUserServiceStatus(response.getStatus().name());
            } else if ("workout-service".equals(response.getService())) {
                user.setWorkoutServiceStatus(response.getStatus().name());
            } else if ("nutrition-service".equals(response.getService())) {
                user.setNutritionServiceStatus(response.getStatus().name());
            }

            userRepository.save(user);
            checkSagaStatus(user);
        });
    }

    private void checkSagaStatus(User user) {
        if ("FAILURE".equals(user.getUserServiceStatus()) || 
            "FAILURE".equals(user.getWorkoutServiceStatus()) ||
            "FAILURE".equals(user.getNutritionServiceStatus())) {
            // Jedan je pao, salji ROLLBACK svima
            log.error("Saga failed for userId={}, sending ROLLBACK", user.getId());
            UserDeletionEvent rollbackEvent = UserDeletionEvent.builder()
                    .userId(user.getId())
                    .type(UserDeletionEvent.Type.ROLLBACK)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.DELETION_EXCHANGE, "user.deletion.request", rollbackEvent);
            
            // Vrati status na ACTIVE
            user.setStatus("ACTIVE");
            user.setUserServiceStatus("PENDING");
            user.setWorkoutServiceStatus("PENDING");
            user.setNutritionServiceStatus("PENDING");
            userRepository.save(user);
        } else if ("SUCCESS".equals(user.getUserServiceStatus()) && 
                   "SUCCESS".equals(user.getWorkoutServiceStatus()) &&
                   "SUCCESS".equals(user.getNutritionServiceStatus())) {
            // Svi su uspjeli, finaliziraj
            log.info("Saga success for userId={}, finalising deletion", user.getId());
            
            UserDeletionEvent finalizeEvent = UserDeletionEvent.builder()
                    .userId(user.getId())
                    .type(UserDeletionEvent.Type.FINALIZE)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.DELETION_EXCHANGE, "user.deletion.request", finalizeEvent);

            // Obrisi korisnika iz auth baze
            userRepository.delete(user);
        }
    }
}
