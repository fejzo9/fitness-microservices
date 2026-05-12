package com.app.fitness.service;

import com.app.fitness.dto.WorkoutPlanRequest;
import com.app.fitness.dto.WorkoutPlanResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.WorkoutPlanMapper;
import com.app.fitness.repository.WorkoutPlanRepository;
import com.fitness.workoutservice.model.WorkoutPlan;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutPlanMapper workoutPlanMapper;

    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> findAll() {
        return workoutPlanRepository.findAll().stream()
                .map(workoutPlanMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> findByUserId(Long userId) {
        return workoutPlanRepository.findByUserId(userId).stream()
                .map(workoutPlanMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutPlanResponse findById(Long id) {
        return workoutPlanRepository.findById(id)
                .map(workoutPlanMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found with id: " + id));
    }

    @Transactional
    public WorkoutPlanResponse create(WorkoutPlanRequest request) {
        if (workoutPlanRepository.existsByUserIdAndName(request.getUserId(), request.getName())) {
            throw new DuplicateResourceException(
                    "Workout plan already exists with name '" + request.getName()
                            + "' for userId=" + request.getUserId());
        }
        WorkoutPlan plan = workoutPlanMapper.toEntity(request);
        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    public WorkoutPlanResponse update(Long id, WorkoutPlanRequest request) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found with id: " + id));
        workoutPlanMapper.updateEntity(request, plan);
        return workoutPlanMapper.toResponse(workoutPlanRepository.save(plan));
    }

    @Transactional
    public void delete(Long id) {
        if (!workoutPlanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workout plan not found with id: " + id);
        }
        workoutPlanRepository.deleteById(id);
    }
}
