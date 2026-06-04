# Fitness Microservices


This repository contains an academic project for the course **Advanced Web Technologies** (*Napredne Web Tehnologije*) in the master's program at the **Faculty of Electrical Engineering, University of Sarajevo**.

## Project Overview

The system is organized as a set of microservices for fitness domain management. Each service has its own PostgreSQL database and runs as an independent Spring Boot application.

Architecture includes:
- `auth-service` – user and role management (authentication).
- `notification-service` – user notification management.
- `user-service` – fitness goals and trainer-client relationships.
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
├── auth-service/
├── notification-service/
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
| auth-service | 8080 | 8084 |
| notification-service | 8080 | 8085 |
| user-db (PostgreSQL) | 5432 | 5433 |
| nutrition-db (PostgreSQL) | 5432 | 5434 |
| workout-db (PostgreSQL) | 5432 | 5435 |
| auth-db (PostgreSQL) | 5432 | 5436 |
| notification-db (PostgreSQL) | 5432 | 5437 |
| pgAdmin | 80 | 5050 |

## Accessing PostgreSQL Databases via pgAdmin

pgAdmin is available at **http://localhost:5050** after running Docker Compose.

**Login credentials:**
- Email: `admin@admin.com`
- Password: `admin`

### Adding a Server for Each Database

In pgAdmin, go to **Object → Register → Server** and fill in the **Connection** tab using the following details for each service database:

| Service | Host (inside Docker network) | Port | Database | Username | Password |
|---|---|---|---|---|---|
| auth-service | `auth-db` | `5432` | `auth_db` | `postgres` | `password` |
| user-service | `user-db` | `5432` | `user_db` | `postgres` | `password` |
| notification-service | `notification-db` | `5432` | `notification_db` | `postgres` | `password` |
| nutrition-service | `nutrition-db` | `5432` | `nutrition_db` | `postgres` | `password` |
| workout-service | `workout-db` | `5432` | `workout_db` | `postgres` | `password` |

> Use the **container name** as the host (e.g. `auth-db`), not `localhost`, because pgAdmin runs inside the same Docker network as the databases.

After registering each server, navigate to **Servers → [server name] → Databases → [db name] → Schemas → public → Tables** to browse all tables.

## REST API Endpoints

Each service exposes a full CRUD REST API. All responses use JSON. Validation errors return HTTP 400 with a `fieldErrors` list; not-found errors return HTTP 404; duplicate resource errors return HTTP 409.

### auth-service (port 8084)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/roles` | List all roles |
| GET | `/api/roles/{id}` | Get role by ID |
| POST | `/api/roles` | Create role |
| PUT | `/api/roles/{id}` | Update role |
| DELETE | `/api/roles/{id}` | Delete role |
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

### user-service (port 8081)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/fitness-goals` | List all fitness goals |
| GET | `/api/fitness-goals/{id}` | Get fitness goal by ID |
| POST | `/api/fitness-goals` | Create fitness goal |
| PUT | `/api/fitness-goals/{id}` | Update fitness goal |
| DELETE | `/api/fitness-goals/{id}` | Delete fitness goal |
| GET | `/api/trainer-clients` | List all trainer-client relationships |
| GET | `/api/trainer-clients/{id}` | Get relationship by ID |
| POST | `/api/trainer-clients` | Create relationship |
| PUT | `/api/trainer-clients/{id}` | Update relationship |
| DELETE | `/api/trainer-clients/{id}` | Delete relationship |

### notification-service (port 8085)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/notifications` | List all notifications |
| GET | `/api/notifications/{id}` | Get notification by ID |
| POST | `/api/notifications` | Create notification |
| PUT | `/api/notifications/{id}` | Update notification |
| DELETE | `/api/notifications/{id}` | Delete notification |

### nutrition-service (port 8082)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/meal-logs` | List all meal logs |
| GET | `/api/meal-logs/{id}` | Get meal log by ID |
| POST | `/api/meal-logs` | Create meal log |
| PUT | `/api/meal-logs/{id}` | Update meal log |
| DELETE | `/api/meal-logs/{id}` | Delete meal log |
| GET | `/api/meal-items` | List all meal items |
| GET | `/api/meal-items/{id}` | Get meal item by ID |
| POST | `/api/meal-items` | Create meal item (requires `mealLogId`) |
| PUT | `/api/meal-items/{id}` | Update meal item |
| DELETE | `/api/meal-items/{id}` | Delete meal item |
| GET | `/api/progress-entries` | List all progress entries |
| GET | `/api/progress-entries/{id}` | Get progress entry by ID |
| POST | `/api/progress-entries` | Create progress entry |
| PUT | `/api/progress-entries/{id}` | Update progress entry |
| DELETE | `/api/progress-entries/{id}` | Delete progress entry |

### workout-service (port 8083)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/exercises` | List all exercises |
| POST | `/api/exercises` | Create exercise |
| PUT | `/api/exercises/{id}` | Update exercise |
| DELETE | `/api/exercises/{id}` | Delete exercise |
| GET | `/api/exercise-categories` | List all categories |
| POST | `/api/exercise-categories` | Create category |
| PUT | `/api/exercise-categories/{id}` | Update category |
| DELETE | `/api/exercise-categories/{id}` | Delete category |
| GET | `/api/exercise-category-maps` | List all exercise-category mappings |
| POST | `/api/exercise-category-maps` | Create mapping |
| DELETE | `/api/exercise-category-maps/{id}` | Delete mapping |
| GET | `/api/workout-exercises` | List all workout exercises |
| GET | `/api/workout-exercises/user/{userId}` | List exercises for specific user |
| GET | `/api/workout-exercises/user/{userId}/day/{day}` | List exercises for user on specific day |
| GET | `/api/workout-exercises/user/{userId}/statistics` | Get weekly statistics for user |
| POST | `/api/workout-exercises` | Create a workout exercise |
| PUT | `/api/workout-exercises/{id}` | Update workout exercise |
| DELETE | `/api/workout-exercises/{id}` | Remove exercise |
| GET | `/api/completed-workouts` | List all completed workouts |
| POST | `/api/completed-workouts` | Log a completed workout |
| PUT | `/api/completed-workouts/{id}` | Update completed workout |
| DELETE | `/api/completed-workouts/{id}` | Delete completed workout |
| GET | `/api/completed-exercises` | List all completed exercises |
| POST | `/api/completed-exercises` | Log a completed exercise |
| PUT | `/api/completed-exercises/{id}` | Update completed exercise |
| DELETE | `/api/completed-exercises/{id}` | Delete completed exercise |


## Frontend (fitness-ui)

The `fitness-ui` directory contains a React + Vite application that communicates with the backend through the API Gateway.

### Environment Variables

The application reads the gateway base URL from an environment variable. Before starting the frontend, create a `.env` file inside `fitness-ui/`:

```bash
cp fitness-ui/.env.example fitness-ui/.env
```

| Variable | Description | Default |
|---|---|---|
| `VITE_API_BASE` | Base URL of the API Gateway | `http://localhost:8080` |

> `.env` is listed in `.gitignore` and will not be committed. Use `.env.example` as a template.

### Running the Frontend

```bash
cd fitness-ui
npm install
npm run dev
```

The app will be available at **http://localhost:3000**.

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

## Local Development Without Docker

Each service is an independent Gradle application and can be run from its own directory:

```bash
cd user-service
./gradlew bootRun
```

The same approach applies to `auth-service`, `notification-service`, `nutrition-service`, and `workout-service`.

> Note: For local non-Docker execution, make sure PostgreSQL is running and required `DB_*` environment variables are configured.

## Domain Models by Service

### auth-service
- `User`
- `Role`

### notification-service
- `Notification`

### user-service
- `FitnessGoal`
- `TrainerClient`

### nutrition-service
- `MealItem`
- `MealLog`
- `ProgressEntry`

### workout-service
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
