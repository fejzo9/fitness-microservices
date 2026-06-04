package com.app.fitness.repository;

import com.app.fitness.model.Exercise;
import com.app.fitness.model.WorkoutExercise;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    boolean existsByUserIdAndDayOfWeekAndExercise(Long userId, DayOfWeek dayOfWeek, Exercise exercise);

    @EntityGraph(attributePaths = {"exercise"})
    List<WorkoutExercise> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"exercise"})
    List<WorkoutExercise> findByUserIdAndDayOfWeek(Long userId, DayOfWeek dayOfWeek);

    @Override
    @EntityGraph(attributePaths = {"exercise"})
    List<WorkoutExercise> findAll();

    @Override
    @EntityGraph(attributePaths = {"exercise"})
    Optional<WorkoutExercise> findById(Long id);

    void deleteByUserId(Long userId);
}
