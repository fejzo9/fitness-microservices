package com.app.fitness.config;

import com.app.fitness.repository.RoleRepository;
import com.app.fitness.repository.UserRepository;
import com.fitness.authservice.model.Role;
import com.fitness.authservice.model.User;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(
            RoleRepository roleRepository,
            UserRepository userRepository) {

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
}
