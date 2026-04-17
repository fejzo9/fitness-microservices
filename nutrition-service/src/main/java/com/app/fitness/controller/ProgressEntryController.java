package com.app.fitness.controller;

import com.app.fitness.dto.ProgressEntryRequest;
import com.app.fitness.dto.ProgressEntryResponse;
import com.app.fitness.service.ProgressEntryService;
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
 * REST controller for managing progress entries.
 *
 * <p>Base URL: /api/progress-entries
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/progress-entries         - Retrieve all progress entries</li>
 *   <li>GET    /api/progress-entries/{id}    - Retrieve a progress entry by ID</li>
 *   <li>POST   /api/progress-entries         - Create a new progress entry</li>
 *   <li>PUT    /api/progress-entries/{id}    - Update an existing progress entry</li>
 *   <li>DELETE /api/progress-entries/{id}    - Delete a progress entry by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/progress-entries")
@RequiredArgsConstructor
public class ProgressEntryController {

    private final ProgressEntryService progressEntryService;

    @GetMapping
    public ResponseEntity<List<ProgressEntryResponse>> getAll() {
        return ResponseEntity.ok(progressEntryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgressEntryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(progressEntryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProgressEntryResponse> create(@Valid @RequestBody ProgressEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(progressEntryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgressEntryResponse> update(@PathVariable Long id,
            @Valid @RequestBody ProgressEntryRequest request) {
        return ResponseEntity.ok(progressEntryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        progressEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
