package com.app.fitness.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "completed_workouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Builder.Default
    @OneToMany(mappedBy = "completedWorkout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompletedExercise> completedExercises = new java.util.ArrayList<>();

    // Privremeno polje kako bi Hibernate uklonio NOT NULL ograničenje sa stare kolone
    @Column(name = "workout_plan_id", nullable = true)
    private Long workoutPlanId;
}
