package com.app.fitness.client;

import com.app.fitness.dto.AuthUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @GetMapping("/api/users/{userId}")
    ResponseEntity<AuthUserDto> getUserById(@PathVariable Long userId);
}