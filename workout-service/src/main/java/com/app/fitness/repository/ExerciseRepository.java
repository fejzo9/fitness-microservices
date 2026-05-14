package com.app.fitness.repository;

import com.fitness.workoutservice.model.Exercise;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    boolean existsByName(String name);

    Optional<Exercise> findByName(String name);
    
    Optional<Exercise> findFirstByName(String name);
}
