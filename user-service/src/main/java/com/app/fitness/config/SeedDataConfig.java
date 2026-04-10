package com.app.fitness.config;

import com.app.fitness.repository.FitnessGoalRepository;
import com.app.fitness.repository.NotificationRepository;
import com.app.fitness.repository.RoleRepository;
import com.app.fitness.repository.TrainerClientRepository;
import com.app.fitness.repository.UserRepository;
import com.fitness.userservice.model.FitnessGoal;
import com.fitness.userservice.model.Notification;
import com.fitness.userservice.model.Role;
import com.fitness.userservice.model.TrainerClient;
import com.fitness.userservice.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            FitnessGoalRepository fitnessGoalRepository,
            TrainerClientRepository trainerClientRepository,
            NotificationRepository notificationRepository) {

        return args -> {
            createRoleIfMissing(roleRepository, "ADMIN");
            createRoleIfMissing(roleRepository, "TRAINER");
            createRoleIfMissing(roleRepository, "USER");

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
            Role trainerRole = roleRepository.findByName("TRAINER")
                    .orElseThrow(() -> new IllegalStateException("TRAINER role not found"));
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalStateException("USER role not found"));

            createUserIfMissing(userRepository, "admin1", "admin1@fitapp.com", "admin123hash", adminRole);
            createUserIfMissing(userRepository, "trainer1", "trainer1@fitapp.com", "trainer123hash", trainerRole);
            createUserIfMissing(userRepository, "user1", "user1@fitapp.com", "user123hash", userRole);
            createUserIfMissing(userRepository, "user2", "user2@fitapp.com", "user234hash", userRole);

            User trainer1 = userRepository.findByUsername("trainer1")
                    .orElseThrow(() -> new IllegalStateException("trainer1 not found"));
            User user1 = userRepository.findByUsername("user1")
                    .orElseThrow(() -> new IllegalStateException("user1 not found"));
            User user2 = userRepository.findByUsername("user2")
                    .orElseThrow(() -> new IllegalStateException("user2 not found"));

            createGoalIfMissing(
                    fitnessGoalRepository,
                    user1,
                    "Lose Weight",
                    new BigDecimal("75.00"),
                    true,
                    LocalDate.parse("2026-08-01"));
            createGoalIfMissing(
                    fitnessGoalRepository,
                    user2,
                    "Gain Muscle",
                    new BigDecimal("85.00"),
                    true,
                    LocalDate.parse("2026-09-01"));

            createTrainerClientIfMissing(
                    trainerClientRepository,
                    trainer1,
                    user1,
                    LocalDate.parse("2026-04-01"),
                    "ACTIVE");
            createTrainerClientIfMissing(
                    trainerClientRepository,
                    trainer1,
                    user2,
                    LocalDate.parse("2026-04-02"),
                    "ACTIVE");

            createNotificationIfMissing(notificationRepository, user1, "Welcome to the platform!", "INFO", false);
            createNotificationIfMissing(
                    notificationRepository,
                    user1,
                    "Your trainer assigned a new program.",
                    "ALERT",
                    false);
            createNotificationIfMissing(notificationRepository, user2, "Track your meals daily.", "REMINDER", false);
        };
    }

    private void createRoleIfMissing(RoleRepository roleRepository, String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            roleRepository.save(Role.builder().name(roleName).build());
        }
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            String username,
            String email,
            String passwordHash,
            Role role) {

        if (!userRepository.existsByUsername(username)) {
            userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordHash)
                    .role(role)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    private void createGoalIfMissing(
            FitnessGoalRepository fitnessGoalRepository,
            User user,
            String goalType,
            BigDecimal targetValue,
            boolean isActive,
            LocalDate deadline) {

        if (!fitnessGoalRepository.existsByUserAndGoalTypeAndDeadline(user, goalType, deadline)) {
            fitnessGoalRepository.save(FitnessGoal.builder()
                    .user(user)
                    .goalType(goalType)
                    .targetValue(targetValue)
                    .isActive(isActive)
                    .deadline(deadline)
                    .build());
        }
    }

    private void createTrainerClientIfMissing(
            TrainerClientRepository trainerClientRepository,
            User trainer,
            User client,
            LocalDate startDate,
            String status) {

        if (!trainerClientRepository.existsByTrainerAndClient(trainer, client)) {
            trainerClientRepository.save(TrainerClient.builder()
                    .trainer(trainer)
                    .client(client)
                    .startDate(startDate)
                    .status(status)
                    .build());
        }
    }

    private void createNotificationIfMissing(
            NotificationRepository notificationRepository,
            User user,
            String message,
            String type,
            boolean isRead) {

        if (!notificationRepository.existsByUserAndMessageAndType(user, message, type)) {
            notificationRepository.save(Notification.builder()
                    .user(user)
                    .message(message)
                    .type(type)
                    .isRead(isRead)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
