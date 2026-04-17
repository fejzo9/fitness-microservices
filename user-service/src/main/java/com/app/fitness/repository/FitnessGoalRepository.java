package com.app.fitness.repository;

import com.fitness.userservice.model.FitnessGoal;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitnessGoalRepository extends JpaRepository<FitnessGoal, Long> {

    boolean existsByUserIdAndGoalTypeAndDeadline(Long userId, String goalType, LocalDate deadline);
}
