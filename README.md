# Volleyflow

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
port: 5432
```

The application uses environment variable placeholders with local demo fallbacks:

```text
DB_URL=jdbc:postgresql://localhost:5432/volleyflow
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
password: password
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
