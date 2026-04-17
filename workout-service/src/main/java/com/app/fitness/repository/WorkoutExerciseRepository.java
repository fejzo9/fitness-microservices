package com.app.fitness.repository;

import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.WorkoutDay;
import com.fitness.workoutservice.model.WorkoutExercise;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    boolean existsByWorkoutDayAndExercise(WorkoutDay workoutDay, Exercise exercise);

    @Override
    @EntityGraph(attributePaths = {"workoutDay", "exercise"})
    List<WorkoutExercise> findAll();

    @Override
    @EntityGraph(attributePaths = {"workoutDay", "exercise"})
    Optional<WorkoutExercise> findById(Long id);
}
