# Volleyflow

Volleyflow to REST API do zarządzania amatorskim klubem siatkarskim. Aplikacja
obsługuje konta użytkowników, profile zawodników, członkostwa w klubach, mecze,
sety oraz statystyki meczowe. Zasoby są identyfikowane w API przez UUID.

## Funkcje

- Rejestracja i logowanie użytkownika z uwierzytelnianiem JWT.
- Zarządzanie kontem i profilem użytkownika.
- Tworzenie klubów oraz zarządzanie ich członkami.
- Role członków klubu: `OWNER`, `TRAINER`, `PLAYER`, `STATISTIC`.
- Tworzenie i aktualizowanie meczów, przypisywanie klubów i sezonów.
- Rejestrowanie wyników setów, nagrań wideo, MVP i statystyk zawodników.
- Pobieranie statystyk meczu, setu i kariery zawodnika.
- Walidacja żądań i ujednolicone odpowiedzi błędów.

## Technologie

- Java 17, Spring Boot 4
- Spring Web MVC, Spring Data JPA, Spring Security
- PostgreSQL i Flyway
- JWT (`jjwt`)
- Maven, Docker Compose
- Springdoc OpenAPI

## Uruchomienie lokalne

Wymagane: Java 17, Docker z Docker Compose.

Uruchom lokalne usługi infrastruktury:

```bash
docker compose up -d --wait
```

Uruchom aplikację z fikcyjnymi danymi demonstracyjnymi:

```bash
SPRING_CONFIG_LOCATION=classpath:/ ./mvnw spring-boot:run
```

`SPRING_CONFIG_LOCATION=classpath:/` wymusza użycie konfiguracji z zasobów
aplikacji. Na Windows PowerShell:

```powershell
$env:SPRING_CONFIG_LOCATION='classpath:/'
./mvnw.cmd spring-boot:run
```

Aplikacja jest dostępna pod adresem `http://localhost:8080`.

### Usługi lokalne

Docker Compose uruchamia PostgreSQL, Redis, Kafkę w trybie KRaft oraz Mailpit,
który przechwytuje wiadomości SMTP i udostępnia je w przeglądarce.

| Usługa | Adres hosta | Przeznaczenie |
|---|---|---|
| PostgreSQL | `localhost:5534` | Baza `volleyflow_demo` |
| Redis | `localhost:6379` | Magazyn danych tymczasowych |
| Kafka | `localhost:9092` | Broker wiadomości; KRaft nie wymaga ZooKeepera |
| Mailpit SMTP | `localhost:1025` | Lokalny serwer do przechwytywania e-maili |
| Mailpit UI | <http://localhost:8025> | Podgląd przechwyconych wiadomości |

Domyślne ustawienia PostgreSQL:

| Ustawienie | Wartość |
|---|---|
| Host | `localhost` |
| Port hosta | `5534` |
| Baza | `volleyflow_demo` |
| Użytkownik | `admin` |
| Hasło | `admin` |

Porty usług można nadpisać zmiennymi `DB_PORT`, `REDIS_PORT`, `KAFKA_PORT`,
`SMTP_PORT` i `MAILPIT_UI_PORT`. Dane PostgreSQL można zmienić przez `POSTGRES_DB`,
`POSTGRES_USER` i `POSTGRES_PASSWORD`; aplikacja przyjmuje `DB_URL`,
`DB_USERNAME` i `DB_PASSWORD`. Usługi są publikowane wyłącznie na interfejsie
loopback komputera.

### Dane demonstracyjne

Flyway domyślnie ładuje przy starcie aplikacji fikcyjne dane demonstracyjne:

- Aurora Volley oraz kluby Orion Volley i Polaris Volley;
- właściciel klubu, trener i 12 przykładowych zawodników;
- 3 zakończone mecze, 12 setów i statystyki zawodników;
- wybrane MVP oraz 2 zaplanowane mecze.

Konto demonstracyjne:

```text
email: aurora.owner@example.com
hasło: demo1234
```

Fixture jest idempotentny w tym sensie, że ponowne uruchomienie nie nadpisuje
istniejących ani lokalnie zmodyfikowanych danych Aurora.

## API

Dokumentacja OpenAPI i interaktywny Swagger UI są dostępne po uruchomieniu:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Wszystkie endpointy poza rejestracją, logowaniem i dokumentacją wymagają tokenu
JWT. Przekaż go w nagłówku:

```http
Authorization: Bearer <jwt-token>
```

### Uwierzytelnianie

| Metoda | Endpoint | Działanie |
|---|---|---|
| `POST` | `/api/auth/register` | Rejestruje konto i tworzy jego profil; zwraca token JWT. |
| `POST` | `/api/auth/login` | Uwierzytelnia użytkownika i zwraca token JWT. |

Przykład rejestracji:

```json
{
  "email": "player@example.com",
  "password": "secret123",
  "phone": "+48123123123",
  "firstName": "Jan",
  "lastName": "Przykładowy",
  "displayName": "Jan P.",
  "jumpCm": 45
}
```

### Konto i profil

| Metoda | Endpoint | Działanie |
|---|---|---|
| `GET` | `/api/users/me` | Pobiera dane zalogowanego konta. |
| `PUT` | `/api/users/me` | Aktualizuje dane konta. |
| `PUT` | `/api/users/me/password` | Zmienia hasło. |
| `DELETE` | `/api/users/me` | Usuwa/dezaktywuje konto. |
| `POST` | `/api/profiles` | Tworzy profil zalogowanego użytkownika. |
| `GET` | `/api/profiles/me` | Pobiera własny profil. |
| `GET` | `/api/profiles/{profileExternalId}` | Pobiera profil po UUID. |
| `PUT` | `/api/profiles` | Aktualizuje własny profil. |
| `DELETE` | `/api/profiles/me` | Usuwa własny profil, jeśli nie jest używany. |

### Kluby

| Metoda | Endpoint | Działanie |
|---|---|---|
| `POST` | `/api/clubs` | Tworzy klub; twórca otrzymuje członkostwo właściciela. |
| `GET` | `/api/clubs/my-clubs` | Zwraca kluby zalogowanego użytkownika. |
| `GET` | `/api/clubs/search?query={tekst}` | Wyszukuje kluby po nazwie. |
| `GET` | `/api/clubs/{clubExternalId}` | Pobiera podstawowe informacje o klubie. |
| `GET` | `/api/clubs/{clubExternalId}/details` | Pobiera szczegóły klubu i jego członków. |
| `PUT` | `/api/clubs/{clubExternalId}` | Aktualizuje klub zgodnie z uprawnieniami. |
| `DELETE` | `/api/clubs/{clubExternalId}` | Dezaktywuje klub zgodnie z uprawnieniami. |

### Członkostwa i zawodnicy

| Metoda | Endpoint | Działanie |
|---|---|---|
| `POST` | `/api/memberships` | Dodaje członka do klubu. |
| `GET` | `/api/memberships/{clubExternalId}/players` | Pobiera zawodników klubu; opcjonalnie `?active=true`. |
| `GET` | `/api/memberships/{clubExternalId}/members` | Pobiera członków klubu; opcjonalnie `?active=true`. |
| `GET` | `/api/memberships/{clubExternalId}/{membershipExternalId}` | Pobiera szczegóły członkostwa. |
| `PUT` | `/api/memberships/{clubExternalId}/{membershipExternalId}` | Aktualizuje członkostwo. |
| `DELETE` | `/api/memberships/{clubExternalId}/{membershipExternalId}` | Usuwa członkostwo. |

### Mecze i statystyki

Endpointy meczowe mają bazową ścieżkę `/internal/matches`.

| Metoda | Endpoint | Działanie |
|---|---|---|
| `POST` | `/internal/matches` | Tworzy mecz. |
| `PUT` | `/internal/matches/{matchExternalId}` | Aktualizuje mecz. |
| `GET` | `/internal/matches?clubExternalId={uuid}&season={rok}` | Pobiera mecze klubu; parametr `season` jest opcjonalny. |
| `GET` | `/internal/matches/{clubExternalId}/{matchExternalId}` | Pobiera szczegóły meczu. |
| `PUT` | `/internal/matches/{clubExternalId}/{matchExternalId}/mvp` | Ustawia MVP meczu. |
| `POST` | `/internal/matches/{clubExternalId}/{matchExternalId}/set` | Dodaje set do meczu. |
| `PUT` | `/internal/matches/{clubExternalId}/{matchExternalId}/set/{setExternalId}` | Aktualizuje wynik setu. |
| `PUT` | `/internal/matches/{clubExternalId}/{matchExternalId}/set/{setExternalId}/video` | Ustawia link do nagrania setu. |
| `POST` | `/internal/matches/{matchExternalId}/finish` | Kończy mecz. |
| `DELETE` | `/internal/matches/{clubExternalId}/{matchExternalId}/set/{setExternalId}` | Usuwa set. |
| `GET` | `/internal/matches/{clubExternalId}/{matchExternalId}/set/{setExternalId}/statistics` | Pobiera statystyki setu. |
| `PUT` | `/internal/matches/{clubExternalId}/{matchExternalId}/set/{setExternalId}/statistics` | Zapisuje statystyki setu. |
| `GET` | `/internal/matches/clubs/{clubExternalId}/players/{playerExternalId}/statistics` | Pobiera statystyki kariery zawodnika w klubie. |
| `GET` | `/internal/matches/clubs/{clubExternalId}/getSeasons` | Pobiera sezony dostępne dla klubu. |

Przykładowe żądanie utworzenia meczu:

```json
{
  "createdByClubExternalId": "2342f555-018b-8171-eb54-e18d1780d0d8",
  "homeClubExternalId": "2342f555-018b-8171-eb54-e18d1780d0d8",
  "awayClubExternalId": "85fac36f-58a8-6d90-e2de-01ce9ca9c514",
  "scheduledAt": "2026-12-12T16:00:00Z",
  "matchLocation": {
    "city": "Miasto Demo",
    "zipCode": "00-001",
    "street": "Sportowa",
    "buildingNumber": "1"
  },
  "season": 2026
}
```

## Błędy API

Błędy walidacji zawierają status HTTP, komunikat i listę błędów pól. Przykład:

```json
{
  "httpStatus": 400,
  "message": "Validation failed",
  "fieldErrors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "invalid email format"
    }
  ]
}
```

Hasła nie są zwracane w `rejectedValue`. Brak uwierzytelnienia zwraca `401`,
brak uprawnień `403`, nieznany zasób `404`, a konflikt danych `409`.

## Testy

Uruchom testy i weryfikację projektu:

```bash
./mvnw clean verify
```

Testy repozytoriów korzystają z H2. Lokalne uruchomienie aplikacji korzysta
z PostgreSQL i Compose oraz domyślnie ładuje dane demonstracyjne.
