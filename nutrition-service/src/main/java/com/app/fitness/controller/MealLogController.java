package com.app.fitness.controller;

import com.app.fitness.dto.MealLogRequest;
import com.app.fitness.dto.MealLogResponse;
import com.app.fitness.service.MealLogService;
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
 * REST controller for managing meal logs.
 *
 * <p>Base URL: /api/meal-logs
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/meal-logs         - Retrieve all meal logs</li>
 *   <li>GET    /api/meal-logs/{id}    - Retrieve a meal log by ID</li>
 *   <li>POST   /api/meal-logs         - Create a new meal log</li>
 *   <li>PUT    /api/meal-logs/{id}    - Update an existing meal log</li>
 *   <li>DELETE /api/meal-logs/{id}    - Delete a meal log by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/meal-logs")
@RequiredArgsConstructor
public class MealLogController {

    private final MealLogService mealLogService;

    @GetMapping
    public ResponseEntity<List<MealLogResponse>> getAll() {
        return ResponseEntity.ok(mealLogService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealLogResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mealLogService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MealLogResponse> create(@Valid @RequestBody MealLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealLogService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealLogResponse> update(@PathVariable Long id,
            @Valid @RequestBody MealLogRequest request) {
        return ResponseEntity.ok(mealLogService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mealLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
