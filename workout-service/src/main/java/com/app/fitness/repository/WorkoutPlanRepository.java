package com.app.fitness.repository;

import com.fitness.workoutservice.model.WorkoutPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    boolean existsByUserIdAndName(Long userId, String name);

    Optional<WorkoutPlan> findByUserIdAndName(Long userId, String name);
    
    Optional<WorkoutPlan> findFirstByUserIdAndName(Long userId, String name);
    
    List<WorkoutPlan> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
