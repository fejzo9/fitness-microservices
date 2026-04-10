package com.app.fitness.repository;

import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.WorkoutDay;
import com.fitness.workoutservice.model.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    boolean existsByWorkoutDayAndExercise(WorkoutDay workoutDay, Exercise exercise);
}
