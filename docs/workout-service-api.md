# Workout Service API

## Svrha servisa
`workout-service` upravlja katalogom vjezbi, kategorijama, vjezbama za treninge korisnika i evidencijom odradenih treninga. Servis takoder sadrzi pomocne endpoint-e za load testing i health provjeru instance.

## Base URL
`http://localhost:8083` kada se servis pokrece kroz `docker-compose`.

Napomena: unutar samog servisa je konfiguriran `server.port=8081`, pa je standalone base URL `http://localhost:8081`.

## Endpoint tabela

### Exercises

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/exercises` | Vraca paginiranu listu vjezbi. Podrzani query parametri: `page`, `size`, `sort`. |
| GET | `/api/exercises/{id}` | Vraca vjezbu po ID-u. |
| POST | `/api/exercises` | Kreira novu vjezbu. |
| PUT | `/api/exercises/{id}` | Azurira postojecu vjezbu. |
| DELETE | `/api/exercises/{id}` | Brise vjezbu. Vraca `204 No Content`. |

### Exercise Categories

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/exercise-categories` | Vraca sve kategorije vjezbi. |
| GET | `/api/exercise-categories/{id}` | Vraca kategoriju po ID-u. |
| POST | `/api/exercise-categories` | Kreira novu kategoriju. |
| PUT | `/api/exercise-categories/{id}` | Azurira postojecu kategoriju. |
| DELETE | `/api/exercise-categories/{id}` | Brise kategoriju. Vraca `204 No Content`. |

### Exercise Category Maps

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/exercise-category-maps` | Vraca sve veze izmedu vjezbi i kategorija. |
| GET | `/api/exercise-category-maps/category/{categoryId}` | Vraca veze po ID-u kategorije. |
| GET | `/api/exercise-category-maps/exercise/{exerciseId}` | Vraca veze po ID-u vjezbe. |
| GET | `/api/exercise-category-maps/{id}` | Vraca jednu vezu po ID-u. |
| POST | `/api/exercise-category-maps` | Kreira novu vezu izmedu vjezbe i kategorije. |
| DELETE | `/api/exercise-category-maps/{id}` | Brise vezu. Vraca `204 No Content`. |

### Workout Exercises

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/workout-exercises` | Vraca sve vjezbe dodijeljene korisnicima. |
| GET | `/api/workout-exercises/{id}` | Vraca workout exercise po ID-u. |
| GET | `/api/workout-exercises/user/{userId}` | Vraca sve vjezbe za korisnika. |
| GET | `/api/workout-exercises/user/{userId}/day/{day}` | Vraca vjezbe za korisnika za odredeni dan (npr. `MONDAY`). |
| GET | `/api/workout-exercises/user/{userId}/statistics` | Vraca nedjeljnu statistiku za korisnika. |
| POST | `/api/workout-exercises` | Dodjeljuje vjezbu korisniku. |
| PUT | `/api/workout-exercises/{id}` | Azurira workout exercise zapis. |
| DELETE | `/api/workout-exercises/{id}` | Brise workout exercise zapis. Vraca `204 No Content`. |

### Completed Workouts

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/completed-workouts` | Vraca sve odradene treninge. |
| GET | `/api/completed-workouts/user/{userId}` | Vraca odradene treninge za korisnika. |
| GET | `/api/completed-workouts/{id}` | Vraca odradeni trening po ID-u. |
| POST | `/api/completed-workouts` | Kreira novi zapis odradenog treninga. |
| PUT | `/api/completed-workouts/{id}` | Azurira zapis odradenog treninga. |
| DELETE | `/api/completed-workouts/{id}` | Brise zapis odradenog treninga. Vraca `204 No Content`. |

### Completed Exercises

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/completed-exercises` | Vraca sve odradene vjezbe. |
| GET | `/api/completed-exercises/exercise/{exerciseId}` | Vraca odradene vjezbe po exercise ID-u. |
| GET | `/api/completed-exercises/user/{userId}` | Vraca odradene vjezbe za korisnika. |
| GET | `/api/completed-exercises/{id}` | Vraca odradenu vjezbu po ID-u. |
| POST | `/api/completed-exercises` | Kreira novi zapis odradene vjezbe. |
| PUT | `/api/completed-exercises/{id}` | Azurira zapis odradene vjezbe. |
| DELETE | `/api/completed-exercises/{id}` | Brise zapis odradene vjezbe. Vraca `204 No Content`. |

### Load Test

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/load-test` | Vraca info o instanci i broju obradenih requestova za load test. |
| GET | `/api/load-test/health` | Vraca health status i broj ukupno obradenih load-test requestova. |

## Primjer request/response (success)

### POST `/api/workout-plans`

Request:

```json
{
  "userId": 25,
  "name": "Push Pull Legs",
  "description": "Trodnevni plan za snagu i hipertrofiju",
  "isActive": true
}
```

Response `201 Created`:

```json
{
  "id": 301,
  "userId": 25,
  "name": "Push Pull Legs",
  "description": "Trodnevni plan za snagu i hipertrofiju",
  "isActive": true
}
```

### GET `/api/exercises?page=0&size=2&sort=id,asc`

Response `200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Bench Press",
      "description": "Chest compound movement",
      "difficulty": "INTERMEDIATE"
    },
    {
      "id": 2,
      "name": "Squat",
      "description": "Lower body compound movement",
      "difficulty": "INTERMEDIATE"
    }
  ],
  "pageNumber": 0,
  "pageSize": 2,
  "totalElements": 20,
  "totalPages": 10,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

## Primjer request/response (error)

### POST `/api/completed-exercises`

Request:

```json
{
  "completedWorkoutId": null,
  "exerciseId": null,
  "setsDone": 4,
  "repsDone": 10
}
```

Response `400 Bad Request`:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2026-05-14T10:39:00",
  "fieldErrors": [
    "completedWorkoutId: Completed workout ID must not be null",
    "exerciseId: Exercise ID must not be null"
  ]
}
```

## Validacijska pravila

| DTO | Pravila |
| --- | --- |
| `ExerciseRequest` | `name` je obavezan, ne smije biti prazan, max `150` znakova. |
| `ExerciseRequest` | `description` je opcionalan string. |
| `ExerciseRequest` | `difficulty` je opcionalan string, max `20` znakova. |
| `ExerciseCategoryRequest` | `name` je obavezan, ne smije biti prazan, max `100` znakova. |
| `ExerciseCategoryRequest` | `description` je opcionalan string. |
| `ExerciseCategoryMapRequest` | `exerciseId` i `categoryId` su obavezni `Long` ID-evi. |
| `WorkoutExerciseRequest` | `userId`, `dayOfWeek` i `exerciseId` su obavezni. |
| `WorkoutExerciseRequest` | `sets`, `reps` i `restSec` su opcionalni `Integer`. |
| `CompletedWorkoutRequest` | `userId` je obavezan `Long`. |
| `CompletedWorkoutRequest` | `date` je obavezan `LocalDate` u ISO formatu `yyyy-MM-dd`. |
| `CompletedWorkoutRequest` | `durationMin` je opcionalan `Integer`. |
| `CompletedExerciseRequest` | `completedWorkoutId` i `exerciseId` su obavezni `Long` ID-evi. |
| `CompletedExerciseRequest` | `setsDone` i `repsDone` su opcionalni `Integer`. |

## Error format

Svi error response payloadi koriste isti JSON format:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Workout plan not found with id: 999",
  "timestamp": "2026-05-14T10:40:00"
}
```

Napomene:

- `fieldErrors` postoji samo za validacijske greske.
- `404` se koristi za nepostojeci resurs.
- `409` se koristi za duplikate, npr. vec postojecu vjezbu, kategoriju ili mapiranje.
- `500` vraca genericku poruku `An unexpected error occurred`.
