# Fitness Microservices


This repository contains an academic project for the course **Advanced Web Technologies** (*Napredne Web Tehnologije*) in the master's program at the **Faculty of Electrical Engineering, University of Sarajevo**.

## Project Overview

The system is organized as a set of microservices for fitness domain management. Each service has its own PostgreSQL database and runs as an independent Spring Boot application.

Architecture includes:
- `user-service` – user, role, notification, and goal management.
- `nutrition-service` – meal tracking and nutrition progress monitoring.
- `workout-service` – workout plans, exercises, and completed workout tracking.

Each service uses:

- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL

- Docker (for containerization)

## Repository Structure


```text
.
├── docker-compose.yml
├── user-service/
├── nutrition-service/
└── workout-service/
```

## Services and Ports

When started with `docker-compose.yml`, the following services are available:

| Service | Internal Port | Host Port |
|---|---:|---:|
| user-service | 8080 | 8081 |
| nutrition-service | 8080 | 8082 |
| workout-service | 8080 | 8083 |
| user-db (PostgreSQL) | 5432 | 5433 |
| nutrition-db (PostgreSQL) | 5432 | 5434 |
| workout-db (PostgreSQL) | 5432 | 5435 |

## Running the Project

### Prerequisites
- Docker
- Docker Compose

### 1) Clone the repository

```bash
git clone <REPOSITORY_URL>
cd fitness-microservices
```

### 2) Start all services

```bash
docker compose up --build
```

### 3) Stop all services

```bash
docker compose down
```

## Local Development Without Docker (Optional)

Each service is an independent Gradle application and can be run from its own directory:

```bash
cd user-service
./gradlew bootRun
```

The same approach applies to `nutrition-service` and `workout-service`.

> Note: For local non-Docker execution, make sure PostgreSQL is running and required `DB_*` environment variables are configured.

## Domain Models by Service

### user-service
- `User`
- `Role`
- `FitnessGoal`
- `Notification`
- `TrainerClient`

### nutrition-service
- `MealItem`
- `MealLog`
- `ProgressEntry`

### workout-service
- `WorkoutPlan`
- `WorkoutDay`
- `WorkoutExercise`
- `Exercise`
- `ExerciseCategory`
- `ExerciseCategoryMap`
- `CompletedWorkout`
- `CompletedExercise`

## Contributors

- Fejzullah Ždralović
- Emina Sirbubalo
- Armin Begić
- Admir Mehmedagić
