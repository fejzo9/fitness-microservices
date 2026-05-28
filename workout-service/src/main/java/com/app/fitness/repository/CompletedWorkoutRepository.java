package com.app.fitness.repository;

import com.fitness.workoutservice.model.CompletedWorkout;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedWorkoutRepository extends JpaRepository<CompletedWorkout, Long> {

    boolean existsByUserIdAndWorkoutPlanIdAndDate(Long userId, Long workoutPlanId, LocalDate date);

    Optional<CompletedWorkout> findByUserIdAndWorkoutPlanIdAndDate(Long userId, Long workoutPlanId, LocalDate date);
    
    Optional<CompletedWorkout> findFirstByUserIdAndWorkoutPlanIdAndDate(Long userId, Long workoutPlanId, LocalDate date);
    
    List<CompletedWorkout> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
