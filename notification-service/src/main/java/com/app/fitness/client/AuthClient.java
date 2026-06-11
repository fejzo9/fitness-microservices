package com.app.fitness.client;

import com.app.fitness.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthClient {

    @GetMapping("/auth/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
