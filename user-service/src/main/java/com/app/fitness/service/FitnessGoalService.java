package com.app.fitness.service;

import com.app.fitness.dto.FitnessGoalRequest;
import com.app.fitness.dto.FitnessGoalResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.FitnessGoalMapper;
import com.app.fitness.repository.FitnessGoalRepository;
import com.fitness.userservice.model.FitnessGoal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FitnessGoalService {

    private final FitnessGoalRepository fitnessGoalRepository;
    private final FitnessGoalMapper fitnessGoalMapper;

    @Transactional(readOnly = true)
    public List<FitnessGoalResponse> findAll() {
        return fitnessGoalRepository.findAll().stream()
                .map(fitnessGoalMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FitnessGoalResponse findById(Long id) {
        return fitnessGoalRepository.findById(id)
                .map(fitnessGoalMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with id: " + id));
    }

    @Transactional
    public FitnessGoalResponse create(FitnessGoalRequest request) {
        if (fitnessGoalRepository.existsByUserIdAndGoalTypeAndDeadline(
                request.getUserId(), request.getGoalType(), request.getDeadline())) {
            throw new DuplicateResourceException(
                    "Fitness goal already exists for userId=" + request.getUserId()
                            + ", goalType=" + request.getGoalType());
        }
        FitnessGoal goal = fitnessGoalMapper.toEntity(request);
        return fitnessGoalMapper.toResponse(fitnessGoalRepository.save(goal));
    }

    @Transactional
    public FitnessGoalResponse update(Long id, FitnessGoalRequest request) {
        FitnessGoal goal = fitnessGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with id: " + id));
        fitnessGoalMapper.updateEntity(request, goal);
        return fitnessGoalMapper.toResponse(fitnessGoalRepository.save(goal));
    }

    @Transactional
    public void delete(Long id) {
        if (!fitnessGoalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fitness goal not found with id: " + id);
        }
        fitnessGoalRepository.deleteById(id);
    }
}
