package com.fitness.nutritionservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "meal_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_log_id", nullable = false)
    @ToString.Exclude
    private MealLog mealLog;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String foodName;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal quantityG;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal calories;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal proteinG;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal carbsG;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal fatsG;
}
