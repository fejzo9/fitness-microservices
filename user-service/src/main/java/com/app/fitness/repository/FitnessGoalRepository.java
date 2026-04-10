package com.app.fitness.repository;

import com.fitness.userservice.model.FitnessGoal;
import com.fitness.userservice.model.User;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitnessGoalRepository extends JpaRepository<FitnessGoal, Long> {

    boolean existsByUserAndGoalTypeAndDeadline(User user, String goalType, LocalDate deadline);
}
