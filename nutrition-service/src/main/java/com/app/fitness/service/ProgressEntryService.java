package com.app.fitness.service;

import com.app.fitness.dto.ProgressEntryRequest;
import com.app.fitness.dto.ProgressEntryResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.ProgressEntryMapper;
import com.app.fitness.repository.ProgressEntryRepository;
import com.fitness.nutritionservice.model.ProgressEntry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProgressEntryService {

    private final ProgressEntryRepository progressEntryRepository;
    private final ProgressEntryMapper progressEntryMapper;

    @Transactional(readOnly = true)
    public List<ProgressEntryResponse> findAll() {
        return progressEntryRepository.findAll().stream()
                .map(progressEntryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgressEntryResponse findById(Long id) {
        return progressEntryRepository.findById(id)
                .map(progressEntryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Progress entry not found with id: " + id));
    }

    @Transactional
    public ProgressEntryResponse create(ProgressEntryRequest request) {
        if (progressEntryRepository.existsByUserIdAndEntryDate(request.getUserId(), request.getEntryDate())) {
            throw new DuplicateResourceException(
                    "Progress entry already exists for userId=" + request.getUserId()
                            + ", date=" + request.getEntryDate());
        }
        ProgressEntry entry = progressEntryMapper.toEntity(request);
        return progressEntryMapper.toResponse(progressEntryRepository.save(entry));
    }

    @Transactional
    public ProgressEntryResponse update(Long id, ProgressEntryRequest request) {
        ProgressEntry entry = progressEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progress entry not found with id: " + id));
        progressEntryMapper.updateEntity(request, entry);
        return progressEntryMapper.toResponse(progressEntryRepository.save(entry));
    }

    @Transactional
    public void delete(Long id) {
        if (!progressEntryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Progress entry not found with id: " + id);
        }
        progressEntryRepository.deleteById(id);
    }
}
