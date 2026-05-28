package com.app.fitness.repository;

import com.fitness.nutritionservice.model.ProgressEntry;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressEntryRepository extends JpaRepository<ProgressEntry, Long> {

    boolean existsByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    void deleteByUserId(Long userId);
}
