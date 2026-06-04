package com.app.fitness.config;

import com.app.fitness.repository.CompletedExerciseRepository;
import com.app.fitness.repository.CompletedWorkoutRepository;
import com.app.fitness.repository.ExerciseCategoryRepository;
import com.app.fitness.repository.ExerciseRepository;
import com.app.fitness.repository.WorkoutExerciseRepository;
import com.app.fitness.model.CompletedExercise;
import com.app.fitness.model.CompletedWorkout;
import com.app.fitness.model.Exercise;
import com.app.fitness.model.ExerciseCategory;
import com.app.fitness.model.WorkoutExercise;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedWorkoutData(
            ExerciseCategoryRepository exerciseCategoryRepository,
            ExerciseRepository exerciseRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            CompletedWorkoutRepository completedWorkoutRepository,
            CompletedExerciseRepository completedExerciseRepository) {

        return args -> {
            // Only run seed data for primary instance to avoid race conditions
            boolean enableSeedData = Boolean.parseBoolean(System.getenv().getOrDefault("ENABLE_SEED_DATA", "true"));
            if (!enableSeedData) {
                return;
            }
            
            // Re-enabled with safer approach
            createCategoryIfMissing(exerciseCategoryRepository, "Chest");
            createCategoryIfMissing(exerciseCategoryRepository, "Back");
            createCategoryIfMissing(exerciseCategoryRepository, "Legs");
            createCategoryIfMissing(exerciseCategoryRepository, "Cardio");

            createExerciseIfMissing(exerciseRepository, "Bench Press", "Chest strength exercise", "MEDIUM");
            createExerciseIfMissing(exerciseRepository, "Push Up", "Bodyweight chest exercise", "EASY");
            createExerciseIfMissing(exerciseRepository, "Pull Up", "Back bodyweight exercise", "HARD");
            createExerciseIfMissing(exerciseRepository, "Barbell Row", "Back strength exercise", "MEDIUM");
            createExerciseIfMissing(exerciseRepository, "Squat", "Leg compound exercise", "HARD");
            createExerciseIfMissing(
                    exerciseRepository,
                    "Lunges",
                    "Leg exercise for balance and strength",
                    "MEDIUM");
            createExerciseIfMissing(exerciseRepository, "Running", "Cardio endurance activity", "EASY");
            createExerciseIfMissing(exerciseRepository, "Jump Rope", "Cardio coordination exercise", "MEDIUM");

            mapExerciseToCategoryIfMissing(exerciseRepository, exerciseCategoryRepository, "Bench Press", "Chest");
            mapExerciseToCategoryIfMissing(exerciseRepository, exerciseCategoryRepository, "Push Up", "Chest");
            mapExerciseToCategoryIfMissing(exerciseRepository, exerciseCategoryRepository, "Pull Up", "Back");
            mapExerciseToCategoryIfMissing(exerciseRepository, exerciseCategoryRepository, "Barbell Row", "Back");
            mapExerciseToCategoryIfMissing(exerciseRepository, exerciseCategoryRepository, "Squat", "Legs");
            mapExerciseToCategoryIfMissing(exerciseRepository, exerciseCategoryRepository, "Lunges", "Legs");
            mapExerciseToCategoryIfMissing(exerciseRepository, exerciseCategoryRepository, "Running", "Cardio");
            mapExerciseToCategoryIfMissing(exerciseRepository, exerciseCategoryRepository, "Jump Rope", "Cardio");

            Long testUserId = 1L;

            LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
            LocalDate sunday = monday.plusDays(6);

            // Obriši stare workout exercise zapise koji nisu u tekućoj sedmici
            workoutExerciseRepository.findAll().stream()
                    .filter(e -> e.getUserId() != null && e.getUserId().equals(testUserId)
                            && e.getScheduledDate() != null
                            && (e.getScheduledDate().isBefore(monday) || e.getScheduledDate().isAfter(sunday)))
                    .forEach(e -> workoutExerciseRepository.deleteById(e.getId()));

            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    exerciseRepository,
                    testUserId,
                    monday,
                    LocalTime.of(8, 0),
                    "Bench Press",
                    4,
                    10,
                    90);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    exerciseRepository,
                    testUserId,
                    monday,
                    LocalTime.of(8, 45),
                    "Push Up",
                    3,
                    15,
                    60);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    exerciseRepository,
                    testUserId,
                    monday.plusDays(2),
                    LocalTime.of(17, 0),
                    "Pull Up",
                    3,
                    8,
                    90);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    exerciseRepository,
                    testUserId,
                    monday.plusDays(2),
                    LocalTime.of(17, 30),
                    "Barbell Row",
                    4,
                    10,
                    90);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    exerciseRepository,
                    testUserId,
                    monday.plusDays(3),
                    LocalTime.of(10, 0),
                    "Running",
                    1,
                    1,
                    0);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    exerciseRepository,
                    testUserId,
                    monday.plusDays(4),
                    LocalTime.of(18, 0),
                    "Squat",
                    4,
                    12,
                    120);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    exerciseRepository,
                    testUserId,
                    monday.plusDays(4),
                    LocalTime.of(18, 45),
                    "Lunges",
                    3,
                    12,
                    60);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    exerciseRepository,
                    testUserId,
                    monday.plusDays(4),
                    LocalTime.of(19, 15),
                    "Jump Rope",
                    1,
                    1,
                    30);

            CompletedWorkout completedWorkout = createCompletedWorkoutIfMissing(completedWorkoutRepository, testUserId);

            createCompletedExerciseIfMissing(completedExerciseRepository, exerciseRepository, completedWorkout, "Bench Press", 4, 10);
            createCompletedExerciseIfMissing(completedExerciseRepository, exerciseRepository, completedWorkout, "Push Up", 3, 15);
        };
    }

    private void createCategoryIfMissing(ExerciseCategoryRepository repository, String name) {
        if (!repository.existsByName(name)) {
            repository.save(ExerciseCategory.builder().name(name).build());
        }
    }

    private void createExerciseIfMissing(
            ExerciseRepository repository,
            String name,
            String description,
            String difficulty) {

        if (!repository.existsByName(name)) {
            repository.save(Exercise.builder()
                    .name(name)
                    .description(description)
                    .difficulty(difficulty)
                    .build());
        }
    }

    private void mapExerciseToCategoryIfMissing(
            ExerciseRepository exerciseRepository,
            ExerciseCategoryRepository categoryRepository,
            String exerciseName,
            String categoryName) {

        Exercise exercise = exerciseRepository.findFirstByName(exerciseName)
                .orElseThrow(() -> new IllegalStateException("Exercise not found: " + exerciseName));
        ExerciseCategory category = categoryRepository.findFirstByNameOrderById(categoryName)
                .orElseThrow(() -> new IllegalStateException("Category not found: " + categoryName));

        boolean alreadyMapped = exercise.getCategories().stream()
                .anyMatch(c -> c.getId().equals(category.getId()));
        if (!alreadyMapped) {
            exercise.getCategories().add(category);
            exerciseRepository.save(exercise);
        }
    }

    private void addWorkoutExerciseIfMissing(
            WorkoutExerciseRepository workoutExerciseRepository,
            ExerciseRepository exerciseRepository,
            Long userId,
            LocalDate scheduledDate,
            LocalTime startTime,
            String exerciseName,
            int sets,
            int reps,
            int restSec) {

        Exercise exercise = exerciseRepository.findFirstByName(exerciseName)
                .orElseThrow(() -> new IllegalStateException("Exercise not found: " + exerciseName));

        if (!workoutExerciseRepository.existsByUserIdAndScheduledDateAndExercise(userId, scheduledDate, exercise)) {
            workoutExerciseRepository.save(WorkoutExercise.builder()
                    .userId(userId)
                    .scheduledDate(scheduledDate)
                    .startTime(startTime)
                    .exercise(exercise)
                    .sets(sets)
                    .reps(reps)
                    .restSec(restSec)
                    .completed(false)
                    .build());
        }
    }

    private CompletedWorkout createCompletedWorkoutIfMissing(
            CompletedWorkoutRepository repository,
            Long userId) {

        LocalDate date = LocalDate.parse("2026-04-10");

        if (!repository.existsByUserIdAndDate(userId, date)) {
            repository.save(CompletedWorkout.builder()
                    .userId(userId)
                    .date(date)
                    .durationMin(65)
                    .build());
        }

        return repository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new IllegalStateException("Completed workout not found"));
    }

    private void createCompletedExerciseIfMissing(
            CompletedExerciseRepository completedExerciseRepository,
            ExerciseRepository exerciseRepository,
            CompletedWorkout completedWorkout,
            String exerciseName,
            int setsDone,
            int repsDone) {

        Exercise exercise = exerciseRepository.findFirstByName(exerciseName)
                .orElseThrow(() -> new IllegalStateException("Exercise not found: " + exerciseName));

        if (!completedExerciseRepository.existsByCompletedWorkoutAndExercise(completedWorkout, exercise)) {
            completedExerciseRepository.save(CompletedExercise.builder()
                    .completedWorkout(completedWorkout)
                    .exercise(exercise)
                    .setsDone(setsDone)
                    .repsDone(repsDone)
                    .build());
        }
    }
}
