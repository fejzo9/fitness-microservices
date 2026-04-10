package com.app.fitness.repository;

import com.fitness.userservice.model.TrainerClient;
import com.fitness.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerClientRepository extends JpaRepository<TrainerClient, Long> {

    boolean existsByTrainerAndClient(User trainer, User client);
}
