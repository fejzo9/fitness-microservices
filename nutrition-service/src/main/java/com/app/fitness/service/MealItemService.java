package com.app.fitness.service;

import com.app.fitness.dto.MealItemRequest;
import com.app.fitness.dto.MealItemResponse;
import com.app.fitness.dto.PageResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.MealItemMapper;
import com.app.fitness.repository.MealItemRepository;
import com.app.fitness.repository.MealLogRepository;
import com.fitness.nutritionservice.model.MealItem;
import com.fitness.nutritionservice.model.MealLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MealItemService {

    private final MealItemRepository mealItemRepository;
    private final MealLogRepository mealLogRepository;
    private final MealItemMapper mealItemMapper;

    @Transactional(readOnly = true)
    public PageResponse<MealItemResponse> findAll(Pageable pageable) {
        Page<MealItem> page = mealItemRepository.findAll(pageable);
        return PageResponse.of(page.map(mealItemMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public MealItemResponse findById(Long id) {
        return mealItemRepository.findById(id)
                .map(mealItemMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Meal item not found with id: " + id));
    }

    @Transactional
    public MealItemResponse create(MealItemRequest request) {
        MealLog mealLog = mealLogRepository.findById(request.getMealLogId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Meal log not found with id: " + request.getMealLogId()));
        if (mealItemRepository.existsByMealLogAndFoodName(mealLog, request.getFoodName())) {
            throw new DuplicateResourceException(
                    "Food item '" + request.getFoodName() + "' already exists in this meal log");
        }
        MealItem item = mealItemMapper.toEntity(request);
        item.setMealLog(mealLog);
        return mealItemMapper.toResponse(mealItemRepository.save(item));
    }

    @Transactional
    public MealItemResponse update(Long id, MealItemRequest request) {
        MealItem item = mealItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal item not found with id: " + id));
        MealLog mealLog = mealLogRepository.findById(request.getMealLogId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Meal log not found with id: " + request.getMealLogId()));
        mealItemMapper.updateEntity(request, item);
        item.setMealLog(mealLog);
        return mealItemMapper.toResponse(mealItemRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        if (!mealItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meal item not found with id: " + id);
        }
        mealItemRepository.deleteById(id);
    }
}
