package com.app.fitness.controller;

import com.app.fitness.dto.UserProfileRequest;
import com.app.fitness.dto.UserRequest;
import com.app.fitness.dto.UserResponse;
import com.app.fitness.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing users.
 *
 * <p>Base URL: /auth/users
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /auth/users         - Retrieve all users</li>
 *   <li>GET    /auth/users/{id}    - Retrieve a user by ID</li>
 *   <li>POST   /auth/users         - Create a new user</li>
 *   <li>PUT    /auth/users/{id}    - Update an existing user</li>
 *   <li>DELETE /auth/users/{id}    - Delete a user by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id,
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
