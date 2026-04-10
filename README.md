# Fitness Microservices

Ovaj repozitorij sadrži akademski projekat iz predmeta **Napredne Web Tehnologije** na master programu **Elektrotehničkog fakulteta u Sarajevu**.

## Opis projekta

Sistem je organizovan kao skup mikroservisa za upravljanje fitness domenom. Svaki servis ima vlastitu PostgreSQL bazu podataka i pokreće se kao zasebna Spring Boot aplikacija.

Arhitektura obuhvata:
- `user-service` – upravljanje korisnicima, ulogama, notifikacijama i ciljevima.
- `nutrition-service` – praćenje obroka i napretka u ishrani.
- `workout-service` – upravljanje planovima treninga, vježbama i završenim treninzima.

Svaki servis koristi:
- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Docker (za kontejnerizaciju)

## Struktura repozitorija

```text
.
├── docker-compose.yml
├── user-service/
├── nutrition-service/
└── workout-service/
```

## Servisi i portovi

Pokretanjem `docker-compose.yml` podižu se sljedeći servisi:

| Servis | Interni port | Host port |
|---|---:|---:|
| user-service | 8080 | 8081 |
| nutrition-service | 8080 | 8082 |
| workout-service | 8080 | 8083 |
| user-db (PostgreSQL) | 5432 | 5433 |
| nutrition-db (PostgreSQL) | 5432 | 5434 |
| workout-db (PostgreSQL) | 5432 | 5435 |

## Pokretanje projekta

### Preduslovi
- Docker
- Docker Compose

### 1) Kloniranje repozitorija

```bash
git clone <URL_REPOZITORIJA>
cd fitness-microservices
```

### 2) Pokretanje svih servisa

```bash
docker compose up --build
```

### 3) Zaustavljanje servisa

```bash
docker compose down
```

## Lokalni razvoj bez Dockera (opcionalno)

Svaki servis je samostalna Gradle aplikacija i može se pokrenuti zasebno iz vlastitog direktorija:

```bash
cd user-service
./gradlew bootRun
```

Isti princip važi za `nutrition-service` i `workout-service`.

> Napomena: Za lokalno pokretanje bez Dockera potrebno je obezbijediti dostupnu PostgreSQL bazu i odgovarajuće `DB_*` varijable okruženja.

## Modeli po servisima

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

## Kontributori

- Fejzullah Ždralović
- Emina Sirbubalo
- Armin Begić
- Admir Mehmedagić
