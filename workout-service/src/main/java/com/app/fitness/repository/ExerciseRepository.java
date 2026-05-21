package com.app.fitness.repository;

import com.fitness.workoutservice.model.Exercise;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    boolean existsByName(String name);

    Optional<Exercise> findByName(String name);

    @EntityGraph(attributePaths = {"categories"})
    Optional<Exercise> findFirstByName(String name);

    @Override
    @EntityGraph(attributePaths = {"categories"})
    Page<Exercise> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"categories"})
    Optional<Exercise> findById(Long id);

    @Query("SELECT e FROM Exercise e LEFT JOIN FETCH e.categories WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Exercise> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
