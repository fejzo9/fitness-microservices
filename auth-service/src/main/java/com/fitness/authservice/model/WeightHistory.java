package com.fitness.authservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "weight_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeightHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(nullable = false)
    private Integer weight;

    @NotNull
    @Column(nullable = false)
    private LocalDate entryDate;
}
