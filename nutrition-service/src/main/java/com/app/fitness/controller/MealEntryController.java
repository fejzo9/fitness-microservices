package com.app.fitness.controller;

import com.app.fitness.dto.MealEntryRequest;
import com.app.fitness.dto.MealEntryResponse;
import com.app.fitness.service.MealEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing meal entries.
 *
 * <p>Base URL: /api/meal-entries
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/meal-entries              - Retrieve all meal entries</li>
 *   <li>GET    /api/meal-entries/{id}         - Retrieve a meal entry by ID</li>
 *   <li>GET    /api/meal-entries/user/{userId}/date/{date} - Retrieve meal entries by user and date</li>
 *   <li>GET    /api/meal-entries/user/{userId} - Retrieve all meal entries by user</li>
 *   <li>POST   /api/meal-entries              - Create a new meal entry</li>
 *   <li>PUT    /api/meal-entries/{id}         - Update an existing meal entry</li>
 *   <li>DELETE /api/meal-entries/{id}         - Delete a meal entry by ID</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/meal-entries")
@RequiredArgsConstructor
public class MealEntryController {

    private final MealEntryService mealEntryService;

    @GetMapping
    public ResponseEntity<List<MealEntryResponse>> getAll() {
        return ResponseEntity.ok(mealEntryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealEntryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mealEntryService.findById(id));
    }

    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<List<MealEntryResponse>> getByUserIdAndDate(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(mealEntryService.findByUserIdAndDate(userId, date));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MealEntryResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(mealEntryService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<MealEntryResponse>> getByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(mealEntryService.findByUserIdAndDateRange(userId, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<MealEntryResponse> create(@Valid @RequestBody MealEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealEntryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealEntryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MealEntryRequest request) {
        return ResponseEntity.ok(mealEntryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mealEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
