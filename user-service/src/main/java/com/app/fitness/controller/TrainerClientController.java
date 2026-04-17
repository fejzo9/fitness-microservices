package com.app.fitness.controller;

import com.app.fitness.dto.TrainerClientRequest;
import com.app.fitness.dto.TrainerClientResponse;
import com.app.fitness.service.TrainerClientService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing trainer-client relationships.
 *
 * <p>Base URL: /api/trainer-clients
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/trainer-clients         - Retrieve all trainer-client relationships</li>
 *   <li>GET    /api/trainer-clients/{id}    - Retrieve a relationship by ID</li>
 *   <li>POST   /api/trainer-clients         - Create a new trainer-client relationship</li>
 *   <li>PUT    /api/trainer-clients/{id}    - Update an existing relationship</li>
 *   <li>DELETE /api/trainer-clients/{id}    - Delete a relationship by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/trainer-clients")
@RequiredArgsConstructor
public class TrainerClientController {

    private final TrainerClientService trainerClientService;

    @GetMapping
    public ResponseEntity<List<TrainerClientResponse>> getAll() {
        return ResponseEntity.ok(trainerClientService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainerClientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(trainerClientService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TrainerClientResponse> create(@Valid @RequestBody TrainerClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerClientService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainerClientResponse> update(@PathVariable Long id,
            @Valid @RequestBody TrainerClientRequest request) {
        return ResponseEntity.ok(trainerClientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trainerClientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
