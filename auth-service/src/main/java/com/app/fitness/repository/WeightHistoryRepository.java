package com.app.fitness.repository;

import com.fitness.authservice.model.WeightHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WeightHistoryRepository extends JpaRepository<WeightHistory, Long> {
    List<WeightHistory> findByUserIdOrderByEntryDateDesc(Long userId);
    List<WeightHistory> findByUserIdOrderByEntryDateAsc(Long userId);
}
