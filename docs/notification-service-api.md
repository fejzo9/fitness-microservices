# Notification Service API

## Svrha servisa
`notification-service` upravlja korisnickim notifikacijama unutar sustava. Servis cuva sadrzaj poruke, tip notifikacije, status procitanosti i vrijeme kreiranja.

## Base URL
`http://localhost:8085`

## Endpoint tabela

| Method | Path | Opis |
| --- | --- | --- |
| GET | `/api/notifications` | Vraca sve notifikacije. |
| GET | `/api/notifications/{id}` | Vraca notifikaciju po ID-u. |
| POST | `/api/notifications` | Kreira novu notifikaciju. |
| PUT | `/api/notifications/{id}` | Azurira postojecu notifikaciju. |
| DELETE | `/api/notifications/{id}` | Brise notifikaciju. Vraca `204 No Content`. |

## Primjer request/response (success)

### POST `/api/notifications`

Request:

```json
{
  "userId": 25,
  "message": "Your workout plan has been updated.",
  "type": "WORKOUT",
  "isRead": false
}
```

Response `201 Created`:

```json
{
  "id": 88,
  "userId": 25,
  "message": "Your workout plan has been updated.",
  "type": "WORKOUT",
  "isRead": false,
  "createdAt": "2026-05-14T10:36:00"
}
```

## Primjer request/response (error)

### POST `/api/notifications`

Request:

```json
{
  "userId": null,
  "message": "",
  "type": "THIS_TYPE_NAME_IS_LONGER_THAN_FIFTY_CHARACTERS_TOTAL",
  "isRead": null
}
```

Response `400 Bad Request`:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2026-05-14T10:37:00",
  "fieldErrors": [
    "userId: User ID must not be null",
    "message: Message must not be blank",
    "type: Type must not exceed 50 characters",
    "isRead: isRead must not be null"
  ]
}
```

## Validacijska pravila

| DTO | Pravila |
| --- | --- |
| `NotificationRequest` | `userId` je obavezan i mora biti `Long`. |
| `NotificationRequest` | `message` je obavezan i ne smije biti prazan. |
| `NotificationRequest` | `type` je obavezan, ne smije biti prazan, max `50` znakova. |
| `NotificationRequest` | `isRead` je obavezan `Boolean`. |

## Error format

Svi error response payloadi koriste isti JSON format:

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Notification already exists for userId=25, type=WORKOUT",
  "timestamp": "2026-05-14T10:38:00"
}
```

Napomene:

- `fieldErrors` se pojavljuje samo kod validacije.
- `404` znaci da notifikacija ne postoji.
- `409` se koristi kada servis detektira duplikat notifikacije.
- `500` vraca genericku poruku `An unexpected error occurred`.
