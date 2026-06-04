package com.app.fitness.repository;

import com.app.fitness.model.CompletedWorkout;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedWorkoutRepository extends JpaRepository<CompletedWorkout, Long> {

    boolean existsByUserIdAndDate(Long userId, LocalDate date);

    Optional<CompletedWorkout> findByUserIdAndDate(Long userId, LocalDate date);

    List<CompletedWorkout> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
