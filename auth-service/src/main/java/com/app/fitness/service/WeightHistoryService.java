package com.app.fitness.service;

import com.app.fitness.dto.WeightHistoryRequest;
import com.app.fitness.dto.WeightHistoryResponse;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.repository.UserRepository;
import com.app.fitness.repository.WeightHistoryRepository;
import com.fitness.authservice.model.User;
import com.fitness.authservice.model.WeightHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeightHistoryService {

    private final WeightHistoryRepository weightHistoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WeightHistoryResponse> getWeightHistoryByUserId(Long userId) {
        return weightHistoryRepository.findByUserIdOrderByEntryDateAsc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WeightHistoryResponse addWeightEntry(WeightHistoryRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        WeightHistory weightHistory = WeightHistory.builder()
                .user(user)
                .weight(request.getWeight())
                .entryDate(request.getEntryDate())
                .build();

        WeightHistory saved = weightHistoryRepository.save(weightHistory);
        
        // Update user's current weight only if this is the latest entry
        weightHistoryRepository.findByUserIdOrderByEntryDateDesc(user.getId()).stream()
                .findFirst()
                .ifPresent(latest -> {
                    if (latest.getEntryDate().isBefore(request.getEntryDate()) || latest.getEntryDate().isEqual(request.getEntryDate())) {
                        user.setWeight(request.getWeight());
                        userRepository.save(user);
                    }
                });

        return mapToResponse(saved);
    }

    private WeightHistoryResponse mapToResponse(WeightHistory weightHistory) {
        return WeightHistoryResponse.builder()
                .id(weightHistory.getId())
                .userId(weightHistory.getUser().getId())
                .weight(weightHistory.getWeight())
                .entryDate(weightHistory.getEntryDate())
                .build();
    }
}
