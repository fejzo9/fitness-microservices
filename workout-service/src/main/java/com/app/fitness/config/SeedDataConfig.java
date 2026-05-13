package com.app.fitness.config;

import com.app.fitness.repository.CompletedExerciseRepository;
import com.app.fitness.repository.CompletedWorkoutRepository;
import com.app.fitness.repository.ExerciseCategoryMapRepository;
import com.app.fitness.repository.ExerciseCategoryRepository;
import com.app.fitness.repository.ExerciseRepository;
import com.app.fitness.repository.WorkoutDayRepository;
import com.app.fitness.repository.WorkoutExerciseRepository;
import com.app.fitness.repository.WorkoutPlanRepository;
import com.fitness.workoutservice.model.CompletedExercise;
import java.util.List;
import com.fitness.workoutservice.model.CompletedWorkout;
import com.fitness.workoutservice.model.Exercise;
import com.fitness.workoutservice.model.ExerciseCategory;
import com.fitness.workoutservice.model.ExerciseCategoryMap;
import com.fitness.workoutservice.model.WorkoutDay;
import com.fitness.workoutservice.model.WorkoutExercise;
import com.fitness.workoutservice.model.WorkoutPlan;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedWorkoutData(
            ExerciseCategoryRepository exerciseCategoryRepository,
            ExerciseRepository exerciseRepository,
            ExerciseCategoryMapRepository exerciseCategoryMapRepository,
            WorkoutPlanRepository workoutPlanRepository,
            WorkoutDayRepository workoutDayRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            CompletedWorkoutRepository completedWorkoutRepository,
            CompletedExerciseRepository completedExerciseRepository) {

        return args -> {
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

            mapExerciseToCategoryIfMissing(exerciseCategoryMapRepository, exerciseRepository, exerciseCategoryRepository, "Bench Press", "Chest");
            mapExerciseToCategoryIfMissing(exerciseCategoryMapRepository, exerciseRepository, exerciseCategoryRepository, "Push Up", "Chest");
            mapExerciseToCategoryIfMissing(exerciseCategoryMapRepository, exerciseRepository, exerciseCategoryRepository, "Pull Up", "Back");
            mapExerciseToCategoryIfMissing(exerciseCategoryMapRepository, exerciseRepository, exerciseCategoryRepository, "Barbell Row", "Back");
            mapExerciseToCategoryIfMissing(exerciseCategoryMapRepository, exerciseRepository, exerciseCategoryRepository, "Squat", "Legs");
            mapExerciseToCategoryIfMissing(exerciseCategoryMapRepository, exerciseRepository, exerciseCategoryRepository, "Lunges", "Legs");
            mapExerciseToCategoryIfMissing(exerciseCategoryMapRepository, exerciseRepository, exerciseCategoryRepository, "Running", "Cardio");
            mapExerciseToCategoryIfMissing(exerciseCategoryMapRepository, exerciseRepository, exerciseCategoryRepository, "Jump Rope", "Cardio");

            WorkoutPlan plan = createPlanIfMissing(workoutPlanRepository);

            createWorkoutDayIfMissing(workoutDayRepository, plan, "Monday", 1);
            createWorkoutDayIfMissing(workoutDayRepository, plan, "Wednesday", 2);
            createWorkoutDayIfMissing(workoutDayRepository, plan, "Friday", 3);

            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    workoutDayRepository,
                    exerciseRepository,
                    plan,
                    "Monday",
                    "Bench Press",
                    4,
                    10,
                    90);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    workoutDayRepository,
                    exerciseRepository,
                    plan,
                    "Monday",
                    "Push Up",
                    3,
                    15,
                    60);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    workoutDayRepository,
                    exerciseRepository,
                    plan,
                    "Wednesday",
                    "Pull Up",
                    3,
                    8,
                    90);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    workoutDayRepository,
                    exerciseRepository,
                    plan,
                    "Wednesday",
                    "Barbell Row",
                    4,
                    10,
                    90);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    workoutDayRepository,
                    exerciseRepository,
                    plan,
                    "Friday",
                    "Squat",
                    4,
                    12,
                    120);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    workoutDayRepository,
                    exerciseRepository,
                    plan,
                    "Friday",
                    "Lunges",
                    3,
                    12,
                    60);
            addWorkoutExerciseIfMissing(
                    workoutExerciseRepository,
                    workoutDayRepository,
                    exerciseRepository,
                    plan,
                    "Friday",
                    "Jump Rope",
                    1,
                    1,
                    30);

            CompletedWorkout completedWorkout = createCompletedWorkoutIfMissing(completedWorkoutRepository, plan.getId());

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
            ExerciseCategoryMapRepository mapRepository,
            ExerciseRepository exerciseRepository,
            ExerciseCategoryRepository categoryRepository,
            String exerciseName,
            String categoryName) {

        Exercise exercise = exerciseRepository.findByName(exerciseName)
                .orElseThrow(() -> new IllegalStateException("Exercise not found: " + exerciseName));
        ExerciseCategory category = categoryRepository.findFirstByNameOrderById(categoryName)
                .orElseThrow(() -> new IllegalStateException("Category not found: " + categoryName));

        if (!mapRepository.existsByExerciseAndCategory(exercise, category)) {
            mapRepository.save(ExerciseCategoryMap.builder()
                    .exercise(exercise)
                    .category(category)
                    .build());
        }
    }

    private WorkoutPlan createPlanIfMissing(WorkoutPlanRepository repository) {
        Long userId = 3L;
        String planName = "Beginner Strength Plan";

        if (!repository.existsByUserIdAndName(userId, planName)) {
            repository.save(WorkoutPlan.builder()
                    .userId(userId)
                    .name(planName)
                    .description("Weekly beginner workout plan")
                    .isActive(true)
                    .build());
        }

        return repository.findByUserIdAndName(userId, planName)
                .orElseThrow(() -> new IllegalStateException("Workout plan not found"));
    }

    private void createWorkoutDayIfMissing(
            WorkoutDayRepository repository,
            WorkoutPlan workoutPlan,
            String dayName,
            int orderIndex) {

        if (!repository.existsByWorkoutPlanAndDayName(workoutPlan, dayName)) {
            repository.save(WorkoutDay.builder()
                    .workoutPlan(workoutPlan)
                    .dayName(dayName)
                    .orderIndex(orderIndex)
                    .build());
        }
    }

    private void addWorkoutExerciseIfMissing(
            WorkoutExerciseRepository workoutExerciseRepository,
            WorkoutDayRepository workoutDayRepository,
            ExerciseRepository exerciseRepository,
            WorkoutPlan plan,
            String dayName,
            String exerciseName,
            int sets,
            int reps,
            int restSec) {

        WorkoutDay workoutDay = workoutDayRepository.findByWorkoutPlanAndDayName(plan, dayName)
                .orElseThrow(() -> new IllegalStateException("Workout day not found: " + dayName));
        Exercise exercise = exerciseRepository.findByName(exerciseName)
                .orElseThrow(() -> new IllegalStateException("Exercise not found: " + exerciseName));

        if (!workoutExerciseRepository.existsByWorkoutDayAndExercise(workoutDay, exercise)) {
            workoutExerciseRepository.save(WorkoutExercise.builder()
                    .workoutDay(workoutDay)
                    .exercise(exercise)
                    .sets(sets)
                    .reps(reps)
                    .restSec(restSec)
                    .build());
        }
    }

    private CompletedWorkout createCompletedWorkoutIfMissing(
            CompletedWorkoutRepository repository,
            Long workoutPlanId) {

        Long userId = 3L;
        LocalDate date = LocalDate.parse("2026-04-10");

        if (!repository.existsByUserIdAndWorkoutPlanIdAndDate(userId, workoutPlanId, date)) {
            repository.save(CompletedWorkout.builder()
                    .userId(userId)
                    .workoutPlanId(workoutPlanId)
                    .date(date)
                    .durationMin(65)
                    .build());
        }

        return repository.findByUserIdAndWorkoutPlanIdAndDate(userId, workoutPlanId, date)
                .orElseThrow(() -> new IllegalStateException("Completed workout not found"));
    }

    private void createCompletedExerciseIfMissing(
            CompletedExerciseRepository completedExerciseRepository,
            ExerciseRepository exerciseRepository,
            CompletedWorkout completedWorkout,
            String exerciseName,
            int setsDone,
            int repsDone) {

        Exercise exercise = exerciseRepository.findByName(exerciseName)
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
