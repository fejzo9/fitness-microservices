package com.app.fitness.config;

import com.app.fitness.repository.FitnessGoalRepository;
import com.app.fitness.repository.TrainerClientRepository;
import com.fitness.userservice.model.FitnessGoal;
import com.fitness.userservice.model.TrainerClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(
            FitnessGoalRepository fitnessGoalRepository,
            TrainerClientRepository trainerClientRepository) {

        return args -> {
            createGoalIfMissing(
                    fitnessGoalRepository,
                    3L,
                    "Lose Weight",
                    new BigDecimal("75.00"),
                    true,
                    LocalDate.parse("2026-08-01"));
            createGoalIfMissing(
                    fitnessGoalRepository,
                    4L,
                    "Gain Muscle",
                    new BigDecimal("85.00"),
                    true,
                    LocalDate.parse("2026-09-01"));

            createTrainerClientIfMissing(
                    trainerClientRepository,
                    2L,
                    3L,
                    LocalDate.parse("2026-04-01"),
                    "ACTIVE");
            createTrainerClientIfMissing(
                    trainerClientRepository,
                    2L,
                    4L,
                    LocalDate.parse("2026-04-02"),
                    "ACTIVE");
        };
    }

    private void createGoalIfMissing(
            FitnessGoalRepository fitnessGoalRepository,
            Long userId,
            String goalType,
            BigDecimal targetValue,
            boolean isActive,
            LocalDate deadline) {

        if (!fitnessGoalRepository.existsByUserIdAndGoalTypeAndDeadline(userId, goalType, deadline)) {
            fitnessGoalRepository.save(FitnessGoal.builder()
                    .userId(userId)
                    .goalType(goalType)
                    .targetValue(targetValue)
                    .isActive(isActive)
                    .deadline(deadline)
                    .build());
        }
    }

    private void createTrainerClientIfMissing(
            TrainerClientRepository trainerClientRepository,
            Long trainerId,
            Long clientId,
            LocalDate startDate,
            String status) {

        if (!trainerClientRepository.existsByTrainerIdAndClientId(trainerId, clientId)) {
            trainerClientRepository.save(TrainerClient.builder()
                    .trainerId(trainerId)
                    .clientId(clientId)
                    .startDate(startDate)
                    .status(status)
                    .build());
        }
    }
}
