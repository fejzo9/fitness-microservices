package com.app.fitness.client;

import com.app.fitness.dto.AuthUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClientFallback.class);

    @Override
    public ResponseEntity<AuthUserDto> getUserById(Long userId) {
        log.warn("auth-service nije dostupan za userId={}", userId);
        return ResponseEntity.status(503).build();
    }
}