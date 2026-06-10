package com.app.fitness.repository;

import com.app.fitness.model.Exercise;
import com.app.fitness.model.WorkoutExercise;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    boolean existsByUserIdAndScheduledDateAndExercise(Long userId, LocalDate scheduledDate, Exercise exercise);

    @EntityGraph(attributePaths = {"exercise"})
    List<WorkoutExercise> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"exercise"})
    List<WorkoutExercise> findByUserIdAndScheduledDateBetween(Long userId, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"exercise"})
    List<WorkoutExercise> findByUserIdAndScheduledDate(Long userId, LocalDate scheduledDate);

    @EntityGraph(attributePaths = {"exercise"})
    List<WorkoutExercise> findByUserIdAndCompletedTrue(Long userId);

    @Override
    @EntityGraph(attributePaths = {"exercise"})
    List<WorkoutExercise> findAll();

    @Override
    @EntityGraph(attributePaths = {"exercise"})
    Optional<WorkoutExercise> findById(Long id);

    void deleteByUserId(Long userId);
}
