package com.app.fitness.repository;

import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.ExerciseCategory;
import com.fitness.workoutservice.model.ExerciseCategoryMap;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseCategoryMapRepository extends JpaRepository<ExerciseCategoryMap, Long> {

    boolean existsByExerciseAndCategory(Exercise exercise, ExerciseCategory category);

    @Override
    @EntityGraph(attributePaths = {"exercise", "category"})
    List<ExerciseCategoryMap> findAll();

    @Override
    @EntityGraph(attributePaths = {"exercise", "category"})
    Optional<ExerciseCategoryMap> findById(Long id);
    
    @EntityGraph(attributePaths = {"exercise", "category"})
    List<ExerciseCategoryMap> findByCategoryId(Long categoryId);
    
    @EntityGraph(attributePaths = {"exercise", "category"})
    List<ExerciseCategoryMap> findByExerciseId(Long exerciseId);
}
