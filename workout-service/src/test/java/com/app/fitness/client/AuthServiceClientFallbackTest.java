package com.app.fitness.client;

import com.app.fitness.dto.AuthUserDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceClientFallbackTest {

    private final AuthServiceClientFallback fallback = new AuthServiceClientFallback();

    @Test
    void getUserById_shouldReturn503_whenAuthServiceUnavailable() {
        ResponseEntity<AuthUserDto> response = fallback.getUserById(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void getUserById_shouldReturnNullBody_whenAuthServiceUnavailable() {
        ResponseEntity<AuthUserDto> response = fallback.getUserById(1L);

        assertThat(response.getBody()).isNull();
    }

    @Test
    void getUserById_shouldHandleAnyUserId() {
        // Fallback mora raditi za bilo koji userId, ne samo za 1
        ResponseEntity<AuthUserDto> response1 = fallback.getUserById(1L);
        ResponseEntity<AuthUserDto> response2 = fallback.getUserById(999L);

        assertThat(response1.getStatusCode().value()).isEqualTo(503);
        assertThat(response2.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void getUserById_fallbackResponseShouldBeCompatibleWith503Check() {
        // Provjeri da servisna logika koja checkira .value() == 503 ispravno detektuje fallback
        ResponseEntity<AuthUserDto> response = fallback.getUserById(42L);

        boolean isFallback = response.getStatusCode().value() == 503;
        assertThat(isFallback).isTrue();
    }
}