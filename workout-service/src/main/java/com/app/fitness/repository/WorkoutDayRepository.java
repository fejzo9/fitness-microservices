package com.app.fitness.repository;

import com.fitness.workoutservice.model.WorkoutDay;
import com.fitness.workoutservice.model.WorkoutPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, Long> {

    boolean existsByWorkoutPlanAndDayName(WorkoutPlan workoutPlan, String dayName);

    Optional<WorkoutDay> findByWorkoutPlanAndDayName(WorkoutPlan workoutPlan, String dayName);

    @Override
    @EntityGraph(attributePaths = "workoutPlan")
    List<WorkoutDay> findAll();

    @Override
    @EntityGraph(attributePaths = "workoutPlan")
    Optional<WorkoutDay> findById(Long id);
}
