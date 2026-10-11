# Volleyflow

## Backend development (`develop`)

Backend na tym branchu jest wyrównany z `front` (`51e32ba4`): kod aplikacji,
testy i migracje schematu `V1–V8`. Frontend może znajdować się lokalnie
w `frontend/`, ale na `develop` cały ten katalog jest ignorowany przez Git.
Kod frontendu jest utrzymywany na branchu `front` i w repozytorium zespołowym.

### Lokalna baza i fikcyjne dane demo

Docker Compose uruchamia niezależny kontener `volleyflow-develop-db` na porcie
`5434` z wolumenem `volleyflow-develop_postgres-data`. Nie korzysta z baz
`volleyflow-db` ani `volleyflow-team-db`. Domyślne dane połączenia:
`volleyflow` / `admin` / `admin` (baza / użytkownik / hasło).

```bash
docker compose up -d --wait
SPRING_CONFIG_LOCATION=classpath:/ ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

`SPRING_CONFIG_LOCATION` wybiera konfigurację z projektu, aby lokalne pliki
wdrożeniowe w `config/` pozostawione przez inne branche nie kierowały
aplikacji do starej bazy. Backend działa na `http://localhost:8080`.
W PowerShell ustaw `$env:SPRING_CONFIG_LOCATION='classpath:/'` i uruchom
`./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo`.

Profil `demo` dodaje `src/main/resources/db/demo/R__demo_data.sql` do lokalizacji
Flyway. Bez tego profilu wykonują się wyłącznie migracje `V1–V8`, w tym
podstawowe dane demonstracyjne obecne już w historycznej migracji `V1`.
Istniejące migracje nie zostały zmienione. Nowe współdzielone migracje schematu
nazywaj od `V10`, ponieważ repozytorium `volleyflow-team` ma już własne `V9`.

Zestaw demo zawiera wyłącznie fikcyjne dane:

- Aurora Volley oraz przeciwników Orion Volley i Polaris Volley;
- właściciela, trenera i 12 zawodników z pozycjami oraz numerami koszulek;
- 3 zakończone mecze (3:1, 2:3, 3:0), 12 setów i 144 rekordy statystyk;
- MVP zakończonych meczów i 2 zaplanowane spotkania;
- statystyki zgodne z obliczeniami backendu; rezerwowi mają wartości zerowe.

Logowanie do Aurora Volley:

```text
email: aurora.owner@example.com
password: demo1234
```

UUID i daty fixture są stałe. Osoby uruchamiające świeżą bazę z tego samego
commita otrzymają ten sam zestaw danych. Różnice w datach rekordów historycznego
`V1` wynikają z użycia `now()` w tej migracji.

Flyway wykonuje migrację `R__` po raz pierwszy i ponownie po zmianie jej sumy
kontrolnej. Fixture rozpoznaje istniejącego właściciela Aurora po UUID i nie
nadpisuje danych ani nie odtwarza usuniętych rekordów podczas kolejnego wykonania.
Zmiana fixture nie aktualizuje wcześniej załadowanych danych.

Aby świadomie odtworzyć całą **lokalną bazę develop** od zera (usuwa wszystkie
dane tylko w tym projekcie Compose):

```bash
docker compose down -v
docker compose up -d --wait
SPRING_CONFIG_LOCATION=classpath:/ ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

### Lokalny frontend

W obecnym katalogu roboczym frontend z `front` jest już dostępny. W świeżym
klonie można pobrać go bez dodawania do indeksu Git:

```bash
git fetch origin front
git archive origin/front frontend | tar -x
cd frontend
npm ci
npm start
```

Frontend działa na `http://localhost:4200` i korzysta z backendu na porcie
`8080`. Wymaga Node.js zgodnego z Angular 20 (np. Node 22.12+).
Na `develop` nie używaj `git add -f frontend`.

### Weryfikacja

```bash
./mvnw clean verify
```

Testy repozytoriów korzystają z H2; profil `demo` służy do uruchamiania aplikacji
z PostgreSQL, nie do zastępowania konfiguracji testów.

Backend application for managing amateur volleyball clubs, players and club memberships. The project is built as a Spring Boot REST API and is currently an MVP/work-in-progress portfolio project.

## Features

- User registration and login with JWT authentication.
- User self-management: account details, account update, password change and soft delete.
- User profile creation, update, lookup and guarded deletion.
- Club creation, update, details view and soft delete.
- Club owner membership created automatically when a club is created.
- Player membership management inside a club.
- Role-based club membership rules for `OWNER`, `TRAINER`, `PLAYER` and `STATISTIC`.
- Request validation with structured API error responses.
- PostgreSQL schema migrations with Flyway.
- PostgreSQL local environment with Docker Compose.
- OpenAPI UI through Springdoc.

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JWT with `jjwt`
- PostgreSQL
- Flyway
- Maven
- Docker Compose
- Lombok

## Domain Overview

The application is centered around four main areas:

- `UserAccount` - application user used for authentication and account management.
- `PersonProfile` - personal profile connected to a user or player.
- `Club` - volleyball club with lifecycle status.
- `ClubMembership` - relation between a profile and a club with role, shirt number, positions and active dates.

The API exposes external UUID identifiers instead of internal database IDs.

## Project Structure

```text
src/main/java/pl/volleyflow
  auth             authentication API and service
  club             club API, service, repository and DTOs
  clubmembership   club member management
  common           shared utilities
  config           shared configuration and API error handling
  personprofile    user/player profile management
  security         JWT and Spring Security configuration
  user             user account model and service
```

## Running Locally

Start PostgreSQL:

```bash
docker compose up -d
```

Run the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Run tests:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

The API starts on the default Spring Boot port: `8080`.

OpenAPI UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Local Database

The included `docker-compose.yml` starts PostgreSQL with local fallback values:

```text
database: volleyflow
user: admin
password: admin
port: 5434
```

The application uses environment variable placeholders with local demo fallbacks:

```text
DB_URL=jdbc:postgresql://localhost:5434/volleyflow
DB_USERNAME=admin
DB_PASSWORD=admin
JWT_SECRET=<base64-encoded-demo-secret>
```

Flyway migrations are stored in:

```text
src/main/resources/db/migration
```

Seeded demo account:

```text
email: demo.owner@example.com
password: demo
```

## Main Endpoints

Authentication:

- `POST /api/auth/register`
- `POST /api/auth/login`

User:

- `GET /api/users/me`
- `PUT /api/users/me`
- `PUT /api/users/me/password`
- `DELETE /api/users/me`

Profile:

- `POST /api/profiles`
- `GET /api/profiles/me`
- `GET /api/profiles/{profileExternalId}`
- `PUT /api/profiles`
- `DELETE /api/profiles/me`

Club:

- `POST /api/clubs`
- `GET /api/clubs/my-clubs`
- `GET /api/clubs/{clubExternalId}`
- `GET /api/clubs/{clubExternalId}/details`
- `PUT /api/clubs/{clubExternalId}`
- `DELETE /api/clubs/{clubExternalId}`

Membership:

- `POST /api/memberships`
- `GET /api/memberships/{clubExternalId}/players`
- `GET /api/memberships/{clubExternalId}/{membershipExternalId}`
- `PUT /api/memberships/{clubExternalId}/{membershipExternalId}`
- `DELETE /api/memberships/{clubExternalId}/{membershipExternalId}`

## Authentication

Protected endpoints require a bearer token:

```text
Authorization: Bearer <jwt-token>
```

Tokens are returned by the register and login endpoints.

## Error Response

The API returns a simple structured error response:

```json
{
  "httpStatus": 400,
  "message": "Validation failed",
  "fieldErrors": [
    {
      "field": "email",
      "rejectedValue": "wrong-email",
      "message": "invalid email format"
    }
  ]
}
```

For non-validation errors, `fieldErrors` is empty.

## Current Status

The current codebase represents an MVP version of the backend. Core authentication, user account management, profiles, clubs and club memberships are implemented. The project is still actively developed and is intended as a portfolio project rather than a production-ready system.

Planned improvements:

- Tests for main business flows.
- More complete OpenAPI documentation.
- Match module with match schedule, status and results.
- Domain events for selected asynchronous processes.

## Notes For Reviewers

This repository is intended as a backend portfolio project. The goal is to show practical Spring Boot development: layered architecture, REST API design, JPA mappings, JWT security, transaction boundaries, Flyway migrations and basic domain rules for a sports club management system.
