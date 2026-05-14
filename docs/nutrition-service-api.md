# Nutrition Service API

## Svrha servisa
`nutrition-service` upravlja dnevnicima obroka, pojedinacnim stavkama obroka i zapisima napretka korisnika. Servis omogucuje pracenje unosa hrane, nutritivnih vrijednosti i osnovnih metrika napretka kroz vrijeme.

## Base URL
`http://localhost:8082`

## Endpoint tabela

### Meal Logs

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/meal-logs` | Vraca sve dnevnike obroka. |
| GET | `/api/meal-logs/{id}` | Vraca dnevnik obroka po ID-u. |
| POST | `/api/meal-logs` | Kreira novi dnevnik obroka. |
| PUT | `/api/meal-logs/{id}` | Azurira postojeci dnevnik obroka. |
| DELETE | `/api/meal-logs/{id}` | Brise dnevnik obroka. Vraca `204 No Content`. |

### Meal Items

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/meal-items` | Vraca paginiranu listu stavki obroka. Podrzani query parametri: `page`, `size`, `sort`. |
| GET | `/api/meal-items/{id}` | Vraca stavku obroka po ID-u. |
| POST | `/api/meal-items` | Kreira novu stavku obroka. `mealLogId` je obavezan. |
| PUT | `/api/meal-items/{id}` | Azurira postojecu stavku obroka. |
| DELETE | `/api/meal-items/{id}` | Brise stavku obroka. Vraca `204 No Content`. |

### Progress Entries

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/progress-entries` | Vraca sve zapise napretka. |
| GET | `/api/progress-entries/{id}` | Vraca zapis napretka po ID-u. |
| POST | `/api/progress-entries` | Kreira novi zapis napretka. |
| PUT | `/api/progress-entries/{id}` | Azurira postojeci zapis napretka. |
| DELETE | `/api/progress-entries/{id}` | Brise zapis napretka. Vraca `204 No Content`. |

## Primjer request/response (success)

### POST `/api/meal-items`

Request:

```json
{
  "mealLogId": 12,
  "foodName": "Chicken breast",
  "quantityG": 200,
  "calories": 330,
  "proteinG": 62,
  "carbsG": 0,
  "fatsG": 7
}
```

Response `201 Created`:

```json
{
  "id": 44,
  "mealLogId": 12,
  "foodName": "Chicken breast",
  "quantityG": 200,
  "calories": 330,
  "proteinG": 62,
  "carbsG": 0,
  "fatsG": 7
}
```

### GET `/api/meal-items?page=0&size=2&sort=id,asc`

Response `200 OK`:

```json
{
  "content": [
    {
      "id": 44,
      "mealLogId": 12,
      "foodName": "Chicken breast",
      "quantityG": 200,
      "calories": 330,
      "proteinG": 62,
      "carbsG": 0,
      "fatsG": 7
    },
    {
      "id": 45,
      "mealLogId": 12,
      "foodName": "Rice",
      "quantityG": 150,
      "calories": 195,
      "proteinG": 4,
      "carbsG": 42,
      "fatsG": 1
    }
  ],
  "pageNumber": 0,
  "pageSize": 2,
  "totalElements": 10,
  "totalPages": 5,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

## Primjer request/response (error)

### POST `/api/meal-items`

Request:

```json
{
  "mealLogId": null,
  "foodName": "",
  "quantityG": -10,
  "calories": -50,
  "proteinG": 20,
  "carbsG": 5,
  "fatsG": 1
}
```

Response `400 Bad Request`:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2026-05-14T10:34:00",
  "fieldErrors": [
    "mealLogId: Meal log ID must not be null",
    "foodName: Food name must not be blank",
    "quantityG: Quantity must be zero or positive",
    "calories: Calories must be zero or positive"
  ]
}
```

## Validacijska pravila

| DTO | Pravila |
| --- | --- |
| `MealLogRequest` | `userId` je obavezan i mora biti `Long`. |
| `MealLogRequest` | `logDate` je obavezan `LocalDate` u ISO formatu `yyyy-MM-dd`. |
| `MealLogRequest` | `mealType` je obavezan, ne smije biti prazan, max `50` znakova. |
| `MealItemRequest` | `mealLogId` je obavezan i mora biti `Long`. |
| `MealItemRequest` | `foodName` je obavezan, ne smije biti prazan, max `200` znakova. |
| `MealItemRequest` | `quantityG`, `calories`, `proteinG`, `carbsG`, `fatsG` su opcionalni `BigDecimal`, ali ako su poslani moraju biti `>= 0`. |
| `ProgressEntryRequest` | `userId` je obavezan i mora biti `Long`. |
| `ProgressEntryRequest` | `entryDate` je obavezan `LocalDate` u ISO formatu `yyyy-MM-dd`. |
| `ProgressEntryRequest` | `weightKg` i `bodyFatPct` su opcionalni `BigDecimal`, ali ako su poslani moraju biti `>= 0`. |
| `ProgressEntryRequest` | `notes` je opcionalan string, max `2000` znakova. |

## Error format

Svi error response payloadi koriste isti JSON format:

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Meal log already exists for userId=25, date=2026-05-14, mealType=BREAKFAST",
  "timestamp": "2026-05-14T10:35:00"
}
```

Napomene:

- `fieldErrors` postoji samo za `400 VALIDATION_ERROR`.
- `404` se koristi kada trazeni resurs ne postoji.
- `409` se koristi kada poslovna pravila zabrane duplikat unosa.
- `500` vraca genericku poruku `An unexpected error occurred`.
