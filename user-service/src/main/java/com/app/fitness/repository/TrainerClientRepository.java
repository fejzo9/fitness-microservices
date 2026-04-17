package com.app.fitness.repository;

import com.fitness.userservice.model.TrainerClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerClientRepository extends JpaRepository<TrainerClient, Long> {

    boolean existsByTrainerIdAndClientId(Long trainerId, Long clientId);
}
