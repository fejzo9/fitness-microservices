package com.app.fitness.repository;

import com.app.fitness.model.ExerciseCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategory, Long> {

    boolean existsByName(String name);

    Optional<ExerciseCategory> findByName(String name);
    
    Optional<ExerciseCategory> findFirstByName(String name);
    
    List<ExerciseCategory> findAllByName(String name);
    
    @Query("SELECT ec FROM ExerciseCategory ec WHERE ec.name = ?1 ORDER BY ec.id ASC")
    Optional<ExerciseCategory> findFirstByNameOrderById(String name);

    List<ExerciseCategory> findByNameIn(List<String> names);
}
