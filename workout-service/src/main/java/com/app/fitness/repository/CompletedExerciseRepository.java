package com.app.fitness.repository;

import com.fitness.workoutservice.model.CompletedExercise;
import com.fitness.workoutservice.model.CompletedWorkout;
import com.fitness.workoutservice.model.Exercise;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedExerciseRepository extends JpaRepository<CompletedExercise, Long> {

    boolean existsByCompletedWorkoutAndExercise(CompletedWorkout completedWorkout, Exercise exercise);

    @Override
    @EntityGraph(attributePaths = {"completedWorkout", "exercise"})
    List<CompletedExercise> findAll();

    @Override
    @EntityGraph(attributePaths = {"completedWorkout", "exercise"})
    Optional<CompletedExercise> findById(Long id);
    
    @EntityGraph(attributePaths = {"completedWorkout", "exercise"})
    List<CompletedExercise> findByExerciseId(Long exerciseId);
    
    @EntityGraph(attributePaths = {"completedWorkout", "exercise"})
    List<CompletedExercise> findByCompletedWorkoutUserId(Long userId);
}
