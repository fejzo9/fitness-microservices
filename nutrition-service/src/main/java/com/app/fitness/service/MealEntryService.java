package com.app.fitness.service;

import com.app.fitness.dto.MealEntryRequest;
import com.app.fitness.dto.MealEntryResponse;
import com.app.fitness.mapper.MealEntryMapper;
import com.app.fitness.repository.MealEntryRepository;
import com.fitness.nutritionservice.model.MealEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealEntryService {

    private final MealEntryRepository mealEntryRepository;
    private final MealEntryMapper mealEntryMapper;

    public List<MealEntryResponse> findAll() {
        return mealEntryRepository.findAll().stream()
                .map(mealEntryMapper::toResponse)
                .toList();
    }

    public MealEntryResponse findById(Long id) {
        MealEntry entity = mealEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal entry not found with id: " + id));
        return mealEntryMapper.toResponse(entity);
    }

    public List<MealEntryResponse> findByUserIdAndDate(Long userId, LocalDate date) {
        return mealEntryRepository.findByUserIdAndEntryDate(userId, date).stream()
                .map(mealEntryMapper::toResponse)
                .toList();
    }

    public List<MealEntryResponse> findByUserId(Long userId) {
        return mealEntryRepository.findByUserId(userId).stream()
                .map(mealEntryMapper::toResponse)
                .toList();
    }

    public List<MealEntryResponse> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return mealEntryRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(mealEntryMapper::toResponse)
                .toList();
    }

    @Transactional
    public MealEntryResponse create(MealEntryRequest request) {
        MealEntry entity = mealEntryMapper.toEntity(request);
        MealEntry saved = mealEntryRepository.save(entity);
        return mealEntryMapper.toResponse(saved);
    }

    @Transactional
    public MealEntryResponse update(Long id, MealEntryRequest request) {
        MealEntry entity = mealEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal entry not found with id: " + id));
        mealEntryMapper.updateEntity(entity, request);
        MealEntry updated = mealEntryRepository.save(entity);
        return mealEntryMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!mealEntryRepository.existsById(id)) {
            throw new RuntimeException("Meal entry not found with id: " + id);
        }
        mealEntryRepository.deleteById(id);
    }
}
