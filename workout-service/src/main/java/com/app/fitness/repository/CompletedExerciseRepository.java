package com.app.fitness.repository;

import com.fitness.workoutservice.model.CompletedExercise;
import com.fitness.workoutservice.model.CompletedWorkout;
import com.fitness.workoutservice.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedExerciseRepository extends JpaRepository<CompletedExercise, Long> {

    boolean existsByCompletedWorkoutAndExercise(CompletedWorkout completedWorkout, Exercise exercise);
}
