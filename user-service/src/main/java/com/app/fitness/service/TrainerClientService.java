package com.app.fitness.service;

import com.app.fitness.dto.TrainerClientRequest;
import com.app.fitness.dto.TrainerClientResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.mapper.TrainerClientMapper;
import com.app.fitness.repository.TrainerClientRepository;
import com.fitness.userservice.model.TrainerClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerClientService {

    private final TrainerClientRepository trainerClientRepository;
    private final TrainerClientMapper trainerClientMapper;

    @Transactional(readOnly = true)
    public List<TrainerClientResponse> findAll() {
        return trainerClientRepository.findAll().stream()
                .map(trainerClientMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerClientResponse> findAllByTrainerId(Long trainerId) {
        return trainerClientRepository.findAllByTrainerId(trainerId).stream()
                .map(trainerClientMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainerClientResponse findById(Long id) {
        return trainerClientRepository.findById(id)
                .map(trainerClientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer-client relationship not found with id: " + id));
    }

    @Transactional
    public TrainerClientResponse create(TrainerClientRequest request) {
        if (trainerClientRepository.existsByTrainerIdAndClientId(
                request.getTrainerId(), request.getClientId())) {
            throw new DuplicateResourceException(
                    "Trainer-client relationship already exists for trainerId="
                            + request.getTrainerId() + ", clientId=" + request.getClientId());
        }
        TrainerClient entity = trainerClientMapper.toEntity(request);
        return trainerClientMapper.toResponse(trainerClientRepository.save(entity));
    }

    @Transactional
    public TrainerClientResponse update(Long id, TrainerClientRequest request) {
        TrainerClient entity = trainerClientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer-client relationship not found with id: " + id));
        trainerClientMapper.updateEntity(request, entity);
        return trainerClientMapper.toResponse(trainerClientRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!trainerClientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trainer-client relationship not found with id: " + id);
        }
        trainerClientRepository.deleteById(id);
    }
}
