package com.app.fitness.repository;

import com.fitness.userservice.model.FitnessGoal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FitnessGoalRepository extends JpaRepository<FitnessGoal, Long> {

    boolean existsByUserIdAndGoalTypeAndDeadline(Long userId, String goalType, LocalDate deadline);
    
    List<FitnessGoal> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.userId = :userId AND fg.isActive = true ORDER BY fg.id DESC")
    List<FitnessGoal> findActiveGoalsByUserId(@Param("userId") Long userId);
}
