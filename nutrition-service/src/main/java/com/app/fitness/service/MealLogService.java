package com.app.fitness.service;

import com.app.fitness.dto.MealLogRequest;
import com.app.fitness.dto.MealLogResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.MealLogMapper;
import com.app.fitness.repository.MealLogRepository;
import com.fitness.nutritionservice.model.MealLog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MealLogService {

    private final MealLogRepository mealLogRepository;
    private final MealLogMapper mealLogMapper;

    @Transactional(readOnly = true)
    public List<MealLogResponse> findAll() {
        return mealLogRepository.findAll().stream()
                .map(mealLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MealLogResponse findById(Long id) {
        return mealLogRepository.findById(id)
                .map(mealLogMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Meal log not found with id: " + id));
    }

    @Transactional
    public MealLogResponse create(MealLogRequest request) {
        if (mealLogRepository.existsByUserIdAndLogDateAndMealType(
                request.getUserId(), request.getLogDate(), request.getMealType())) {
            throw new DuplicateResourceException(
                    "Meal log already exists for userId=" + request.getUserId()
                            + ", date=" + request.getLogDate()
                            + ", mealType=" + request.getMealType());
        }
        MealLog mealLog = mealLogMapper.toEntity(request);
        return mealLogMapper.toResponse(mealLogRepository.save(mealLog));
    }

    @Transactional
    public MealLogResponse update(Long id, MealLogRequest request) {
        MealLog mealLog = mealLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal log not found with id: " + id));
        mealLogMapper.updateEntity(request, mealLog);
        return mealLogMapper.toResponse(mealLogRepository.save(mealLog));
    }

    @Transactional
    public void delete(Long id) {
        if (!mealLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meal log not found with id: " + id);
        }
        mealLogRepository.deleteById(id);
    }
}
