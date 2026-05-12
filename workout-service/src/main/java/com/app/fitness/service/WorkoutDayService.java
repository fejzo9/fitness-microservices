package com.app.fitness.service;

import com.app.fitness.dto.WorkoutDayRequest;
import com.app.fitness.dto.WorkoutDayResponse;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.WorkoutDayMapper;
import com.app.fitness.repository.WorkoutDayRepository;
import com.app.fitness.repository.WorkoutPlanRepository;
import com.fitness.workoutservice.model.WorkoutDay;
import com.fitness.workoutservice.model.WorkoutPlan;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutDayService {

    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutDayMapper workoutDayMapper;

    @Transactional(readOnly = true)
    public List<WorkoutDayResponse> findAll() {
        return workoutDayRepository.findAll().stream()
                .map(workoutDayMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutDayResponse> findByWorkoutPlanId(Long workoutPlanId) {
        return workoutDayRepository.findByWorkoutPlanId(workoutPlanId).stream()
                .map(workoutDayMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutDayResponse findById(Long id) {
        return workoutDayRepository.findById(id)
                .map(workoutDayMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Workout day not found with id: " + id));
    }

    @Transactional
    public WorkoutDayResponse create(WorkoutDayRequest request) {
        WorkoutPlan plan = workoutPlanRepository.findById(request.getWorkoutPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout plan not found with id: " + request.getWorkoutPlanId()));
        WorkoutDay day = workoutDayMapper.toEntity(request);
        day.setWorkoutPlan(plan);
        return workoutDayMapper.toResponse(workoutDayRepository.save(day));
    }

    @Transactional
    public WorkoutDayResponse update(Long id, WorkoutDayRequest request) {
        WorkoutDay day = workoutDayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout day not found with id: " + id));
        WorkoutPlan plan = workoutPlanRepository.findById(request.getWorkoutPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout plan not found with id: " + request.getWorkoutPlanId()));
        workoutDayMapper.updateEntity(request, day);
        day.setWorkoutPlan(plan);
        return workoutDayMapper.toResponse(workoutDayRepository.save(day));
    }

    @Transactional
    public void delete(Long id) {
        if (!workoutDayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workout day not found with id: " + id);
        }
        workoutDayRepository.deleteById(id);
    }
}
