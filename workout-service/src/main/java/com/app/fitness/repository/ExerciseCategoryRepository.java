package com.app.fitness.repository;

import com.fitness.workoutservice.model.ExerciseCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategory, Long> {

    boolean existsByName(String name);

    Optional<ExerciseCategory> findByName(String name);
}
