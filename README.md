# Volleyflow

Backend application for managing amateur volleyball clubs, players and club memberships. The project is built as a Spring Boot REST API and is currently an MVP/work-in-progress portfolio project.

## Features

- User registration and login with JWT authentication.
- User profile creation.
- Club creation, update, details view and soft delete.
- Club owner membership created automatically when a club is created.
- Player membership management inside a club.
- Role-based club membership rules for `OWNER`, `TRAINER`, `PLAYER` and `STATISTIC`.
- Validation for request payloads and basic business constraints.
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

- `UserAccount` - application user used for authentication.
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
  personprofile    user/player profile management
  security         JWT and Spring Security configuration
  config           shared configuration
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

The API starts on the default Spring Boot port: `8080`.

OpenAPI UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Local Database

The included `docker-compose.yml` starts PostgreSQL with:

```text
database: volleyflow
user: admin
password: admin
port: 5432
```

Current local configuration is stored in `src/main/resources/application.properties`.

## Main Endpoints

Authentication:

- `POST /api/auth/register`
- `POST /api/auth/login`

User:

- `GET /api/users/me`

Profile:

- `POST /api/profiles`

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

## Current Status

The project is still before the final MVP. Core CRUD and membership flows are present, but the application still needs production-grade infrastructure work before being treated as complete.

Planned improvements:

- Tests for main business flows.
- Kafka and domain events for selected asynchronous processes.
- Externalized secrets and environment-based configuration.
- More detailed API error responses.
- More complete OpenAPI documentation.

## Notes For Reviewers

This repository is intended as a backend portfolio project. The goal is to show practical Spring Boot development: layered architecture, REST API design, JPA mappings, JWT security, transaction boundaries and basic domain rules for a sports club management system.
