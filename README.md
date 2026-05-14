# Shift CRM

Shift CRM is a lightweight backend CRM for managing sellers, transactions, and basic analytics.

## What it does

- Full CRUD for sellers.
- Full CRUD for transactions.
- Analytics for the most productive seller by day, month, quarter, and year.
- A query for sellers whose total transaction amount in a period is below a given threshold.
- An extra analytics endpoint that finds the best activity window for a seller by transaction count.
- Centralized error handling with consistent HTTP responses.

## Tech stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Jakarta Validation
- PostgreSQL
- H2 for tests
- Gradle

## Data model

### Seller

- `id` - unique identifier.
- `name` - seller name.
- `contactInfo` - seller contact details.
- `registrationDate` - date and time the seller was created.
- `deletedAt` - soft delete timestamp.

### Transaction

- `id` - unique identifier.
- `seller` - reference to the seller.
- `amount` - transaction amount.
- `paymentType` - payment type: `CASH`, `CARD`, `TRANSFER`.
- `transactionDate` - date and time of the transaction.

## API

### Sellers

- `GET /api/sellers` - list all sellers.
- `GET /api/sellers/{id}` - get seller details.
- `POST /api/sellers` - create a seller.
- `PATCH /api/sellers/{id}` - update a seller.
- `DELETE /api/sellers/{id}` - delete a seller.

### Transactions

- `GET /api/transactions` - list all transactions.
- `GET /api/transactions/{id}` - get transaction details.
- `POST /api/transactions` - create a transaction.
- `GET /api/transactions/seller/{sellerId}` - get all transactions for a seller.

### Analytics

- `GET /api/analytics/most-productive/{period}` - get the most productive seller for `day`, `month`, `quarter`, or `year`.
- `GET /api/analytics/sellers/below-amount?amount=...&from=...&to=...` - get sellers whose total transaction amount is below a given value.
- `GET /api/analytics/best-period/{sellerId}?windowDays=30` - get the best activity period for a seller.

## Request examples

### Create a seller

```http
POST /api/sellers
Content-Type: application/json

{
  "name": "Ivan",
  "contactInfo": "ivan@mail.ru"
}
```

### Create a transaction

```http
POST /api/transactions
Content-Type: application/json

{
  "sellerId": 1,
  "amount": 150.50,
  "paymentType": "CARD"
}
```

### Get the most productive seller for a month

```http
GET /api/analytics/most-productive/month
```

### Get sellers below a threshold

```http
GET /api/analytics/sellers/below-amount?amount=1000&from=2026-05-14%2000:00:00&to=2026-05-14%2023:59:59
```

## Run locally

### Requirements

- Java 21
- Docker and Docker Compose for PostgreSQL

### Start PostgreSQL

The repository includes a `docker-compose.yml` file:

```bash
docker compose up -d
```

### Environment variables

By default the application expects:

- `DB_URL=jdbc:postgresql://localhost:5432/shift_crm`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=postgres`

### Start the application

```bash
./gradlew bootRun
```

The application runs on port `8081`.

## Tests

The project includes unit and integration tests for services, controllers, and the transaction repository.

Run the tests with:

```bash
./gradlew test
```

Tests use an in-memory H2 database.

## Error handling

The global exception handler returns clear HTTP codes and messages for:

- missing entities;
- invalid requests;
- missing parameters;
- malformed request bodies;
- unexpected errors.

## Project structure

- `app/src/main/java/org/example/controller` - REST API layer.
- `app/src/main/java/org/example/service` - business logic.
- `app/src/main/java/org/example/repository` - JPA repositories.
- `app/src/main/java/org/example/model` - entities, DTOs, mappers, projections.
- `app/src/main/java/org/example/exception` - centralized error handling.

## Notes

- Sellers are soft-deleted to preserve history.
- The transaction repository contains custom queries used by the analytics endpoints.