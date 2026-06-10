package com.app.fitness.controller;

import com.app.fitness.dto.WeightHistoryRequest;
import com.app.fitness.dto.WeightHistoryResponse;
import com.app.fitness.service.WeightHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/auth/weight-history")
@RequiredArgsConstructor
public class WeightHistoryController {

    private final WeightHistoryService weightHistoryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WeightHistoryResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(weightHistoryService.getWeightHistoryByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<WeightHistoryResponse> addEntry(@Valid @RequestBody WeightHistoryRequest request) {
        return ResponseEntity.ok(weightHistoryService.addWeightEntry(request));
    }
}
