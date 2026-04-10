package com.app.fitness.repository;

import com.fitness.workoutservice.model.WorkoutDay;
import com.fitness.workoutservice.model.WorkoutPlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, Long> {

    boolean existsByWorkoutPlanAndDayName(WorkoutPlan workoutPlan, String dayName);

    Optional<WorkoutDay> findByWorkoutPlanAndDayName(WorkoutPlan workoutPlan, String dayName);
}
