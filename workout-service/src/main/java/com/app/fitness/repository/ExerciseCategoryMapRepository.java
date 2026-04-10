package com.app.fitness.repository;

import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.ExerciseCategory;
import com.fitness.workoutservice.model.ExerciseCategoryMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseCategoryMapRepository extends JpaRepository<ExerciseCategoryMap, Long> {

    boolean existsByExerciseAndCategory(Exercise exercise, ExerciseCategory category);
}
