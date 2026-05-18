package com.app.fitness.config;

import com.app.fitness.repository.RoleRepository;
import com.app.fitness.repository.UserRepository;
import com.fitness.authservice.model.Role;
import com.fitness.authservice.model.User;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

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

            createUserIfMissing(userRepository, "admin1", "admin1@fitapp.com", "admin123", adminRole, passwordEncoder);
            createUserIfMissing(userRepository, "trainer1", "trainer1@fitapp.com", "trainer123", trainerRole, passwordEncoder);
            createUserIfMissing(userRepository, "user1", "user1@fitapp.com", "user123", userRole, passwordEncoder);
            createUserIfMissing(userRepository, "user2", "user2@fitapp.com", "user234", userRole, passwordEncoder);
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
            String password,
            Role role,
            PasswordEncoder passwordEncoder) {

        if (!userRepository.existsByUsername(username)) {
            userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(role)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
