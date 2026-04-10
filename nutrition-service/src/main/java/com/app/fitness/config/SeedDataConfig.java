package com.app.fitness.config;

import com.app.fitness.repository.MealItemRepository;
import com.app.fitness.repository.MealLogRepository;
import com.app.fitness.repository.ProgressEntryRepository;
import com.fitness.nutritionservice.model.MealItem;
import com.fitness.nutritionservice.model.MealLog;
import com.fitness.nutritionservice.model.ProgressEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(
            MealLogRepository mealLogRepository,
            MealItemRepository mealItemRepository,
            ProgressEntryRepository progressEntryRepository) {
        return args -> {
            MealLog breakfast = upsertMealLog(mealLogRepository, 3L, LocalDate.parse("2026-04-10"), "BREAKFAST");
            MealLog lunch = upsertMealLog(mealLogRepository, 3L, LocalDate.parse("2026-04-10"), "LUNCH");
            MealLog dinner = upsertMealLog(mealLogRepository, 4L, LocalDate.parse("2026-04-10"), "DINNER");

            upsertMealItem(mealItemRepository, breakfast, "Oatmeal", "100.00", "380.00", "13.00", "67.00", "7.00");
            upsertMealItem(mealItemRepository, breakfast, "Banana", "120.00", "105.00", "1.30", "27.00", "0.40");

            upsertMealItem(mealItemRepository, lunch, "Chicken Breast", "200.00", "330.00", "62.00", "0.00", "7.00");
            upsertMealItem(mealItemRepository, lunch, "Rice", "180.00", "230.00", "4.50", "50.00", "0.50");

            upsertMealItem(mealItemRepository, dinner, "Salmon", "180.00", "360.00", "34.00", "0.00", "22.00");
            upsertMealItem(mealItemRepository, dinner, "Salad", "150.00", "80.00", "2.00", "10.00", "3.00");

            upsertProgressEntry(progressEntryRepository, 3L, LocalDate.parse("2026-04-01"), "82.50", "18.20", "Initial measurement");
            upsertProgressEntry(progressEntryRepository, 3L, LocalDate.parse("2026-04-05"), "81.90", "17.90", "Good first week");
            upsertProgressEntry(progressEntryRepository, 3L, LocalDate.parse("2026-04-10"), "81.30", "17.50", "Visible progress");
        };
    }

    private MealLog upsertMealLog(MealLogRepository repository, Long userId, LocalDate date, String mealType) {
        if (!repository.existsByUserIdAndLogDateAndMealType(userId, date, mealType)) {
            repository.save(MealLog.builder().userId(userId).logDate(date).mealType(mealType).build());
        }
        return repository.findByUserIdAndLogDateAndMealType(userId, date, mealType)
                .orElseThrow(() -> new IllegalStateException("Seed meal log not found"));
    }

    private void upsertMealItem(
            MealItemRepository repository,
            MealLog mealLog,
            String foodName,
            String quantity,
            String calories,
            String protein,
            String carbs,
            String fats) {
        if (!repository.existsByMealLogAndFoodName(mealLog, foodName)) {
            repository.save(MealItem.builder()
                    .mealLog(mealLog)
                    .foodName(foodName)
                    .quantityG(new BigDecimal(quantity))
                    .calories(new BigDecimal(calories))
                    .proteinG(new BigDecimal(protein))
                    .carbsG(new BigDecimal(carbs))
                    .fatsG(new BigDecimal(fats))
                    .build());
        }
    }

    private void upsertProgressEntry(
            ProgressEntryRepository repository,
            Long userId,
            LocalDate date,
            String weight,
            String bodyFat,
            String notes) {
        if (!repository.existsByUserIdAndEntryDate(userId, date)) {
            repository.save(ProgressEntry.builder()
                    .userId(userId)
                    .entryDate(date)
                    .weightKg(new BigDecimal(weight))
                    .bodyFatPct(new BigDecimal(bodyFat))
                    .notes(notes)
                    .build());
        }
    }
}
