# User Service API

## Svrha servisa
`user-service` upravlja korisnickim fitness ciljevima i relacijama trener-klijent. Servis podrzava pracenje ciljeva korisnika te evidenciju aktivnih ili povijesnih suradnji izmedu trenera i klijenata.

## Base URL
`http://localhost:8081`

## Endpoint tabela

### Fitness Goals

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/fitness-goals` | Vraca sve fitness ciljeve. |
| GET | `/api/fitness-goals/{id}` | Vraca fitness cilj po ID-u. |
| POST | `/api/fitness-goals` | Kreira novi fitness cilj. |
| PUT | `/api/fitness-goals/{id}` | Azurira postojeci fitness cilj. |
| DELETE | `/api/fitness-goals/{id}` | Brise fitness cilj. Vraca `204 No Content`. |

### Trainer Clients

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/trainer-clients` | Vraca sve trainer-client veze. |
| GET | `/api/trainer-clients/{id}` | Vraca trainer-client vezu po ID-u. |
| POST | `/api/trainer-clients` | Kreira novu trainer-client vezu. |
| PUT | `/api/trainer-clients/{id}` | Azurira postojecu trainer-client vezu. |
| DELETE | `/api/trainer-clients/{id}` | Brise trainer-client vezu. Vraca `204 No Content`. |

## Primjer request/response (success)

### POST `/api/fitness-goals`

Request:

```json
{
  "userId": 25,
  "goalType": "WEIGHT_LOSS",
  "targetValue": 75.5,
  "isActive": true,
  "deadline": "2026-09-01"
}
```

Response `201 Created`:

```json
{
  "id": 101,
  "userId": 25,
  "goalType": "WEIGHT_LOSS",
  "targetValue": 75.5,
  "isActive": true,
  "deadline": "2026-09-01"
}
```

### POST `/api/trainer-clients`

Request:

```json
{
  "trainerId": 7,
  "clientId": 25,
  "startDate": "2026-05-15",
  "status": "ACTIVE"
}
```

Response `201 Created`:

```json
{
  "id": 55,
  "trainerId": 7,
  "clientId": 25,
  "startDate": "2026-05-15",
  "status": "ACTIVE"
}
```

## Primjer request/response (error)

### POST `/api/trainer-clients`

Request:

```json
{
  "trainerId": null,
  "clientId": 25,
  "startDate": null,
  "status": ""
}
```

Response `400 Bad Request`:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2026-05-14T10:32:00",
  "fieldErrors": [
    "trainerId: Trainer ID must not be null",
    "startDate: Start date must not be null",
    "status: Status must not be blank"
  ]
}
```

## Validacijska pravila

| DTO | Pravila |
| --- | --- |
| `FitnessGoalRequest` | `userId` je obavezan i mora biti `Long`. |
| `FitnessGoalRequest` | `goalType` je obavezan, ne smije biti prazan, max `100` znakova. |
| `FitnessGoalRequest` | `targetValue` je opcionalan `BigDecimal`. |
| `FitnessGoalRequest` | `isActive` je obavezan `Boolean`. |
| `FitnessGoalRequest` | `deadline` je opcionalan `LocalDate` u ISO formatu `yyyy-MM-dd`. |
| `TrainerClientRequest` | `trainerId` je obavezan i mora biti `Long`. |
| `TrainerClientRequest` | `clientId` je obavezan i mora biti `Long`. |
| `TrainerClientRequest` | `startDate` je obavezan `LocalDate` u ISO formatu `yyyy-MM-dd`. |
| `TrainerClientRequest` | `status` je obavezan, ne smije biti prazan, max `20` znakova. |

## Error format

Svi error response payloadi koriste isti JSON format:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Fitness goal not found with id: 999",
  "timestamp": "2026-05-14T10:33:00"
}
```

Napomene:

- `fieldErrors` se pojavljuje samo kod `400 VALIDATION_ERROR`.
- `404` oznacava da trazeni zapis ne postoji.
- `409` se vraca kada servis prepozna duplikat postojeceg zapisa.
- `500` vraca poruku `An unexpected error occurred`.
