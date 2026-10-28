# VolleyFlow

VolleyFlow to aplikacja do prowadzenia amatorskiego klubu siatkarskiego: od składu
i terminarza po wyniki setów oraz statystyki zawodników. Repozytorium zawiera API
w Spring Boot oraz frontend Angular, dzięki któremu można obejrzeć aplikację bez
ręcznego wysyłania żądań.

**Mój główny obszar pracy to backend:** model domenowy, API, uprawnienia,
przechowywanie danych i integracje. Frontend służy jako interfejs demonstracyjny;
powstał i jest rozwijany przy wsparciu narzędzi AI. Dane przykładowe są fikcyjne.

## Demo w kilka minut

Potrzebujesz **Java 17**, **Dockera z Docker Compose** oraz **Node.js 20.19+
(lub 22.12+) z npm**. W katalogu głównym repozytorium uruchom:

```bash
docker compose up -d --wait
SPRING_CONFIG_LOCATION=classpath:/ ./mvnw spring-boot:run
```

W **drugim terminalu**:

```bash
cd frontend
npm ci
npm start
```

Otwórz <http://localhost:4200> i zaloguj się:

```text
e-mail: aurora.owner@example.com
hasło:  demo1234
```

**Co obejrzeć po zalogowaniu:** Aurora Volley i jej skład, terminarz z trzema
zakończonymi meczami, szczegóły setów, statystyki zawodników i centrum statystyk.
Możesz też utworzyć własne konto i przejść aktywację przez lokalny Mailpit
(<http://localhost:8025>). Demo działa **lokalnie**; repozytorium nie udostępnia
publicznej instancji aplikacji.

> Konto demo ma stałe hasło i uprawnienia właściciela klubu. Jest przeznaczone
> wyłącznie do lokalnego środowiska demonstracyjnego, nie do publicznego wdrożenia
> z udostępnioną bazą danych. Domyślny sekret JWT i hasła infrastruktury są
> ustawieniami lokalnymi — przy wdrożeniu należy podać własne sekrety.

## Co pokazuje projekt

- **Procesy konta:** rejestracja, aktywacja przez e-mail, logowanie JWT oraz
  reset hasła tokenem ważnym przez 15 minut.
- **Domena klubu:** profile, członkostwa z rolami, mecze, sety, MVP oraz
  statystyki setów i kariery zawodnika.
- **Integracje:** PostgreSQL i migracje Flyway dla danych trwałych, Redis dla
  krótkotrwałych tokenów, Kafka do zlecania wysyłki e-maili, Mailpit do
  lokalnego podglądu wiadomości.
- **Bezpieczeństwo:** autoryzacja operacji na zasobach klubu, walidacja żądań
  i wspólny format błędów API.

Szczegółowe instrukcje uruchomienia, endpointy i informacje o testach są poniżej.

## Technologie

- Java 17, Spring Boot 4
- Spring Web MVC, Spring Data JPA, Spring Security
- PostgreSQL, Flyway, Redis i Kafka
- JWT (`jjwt`)
- Angular 20, TypeScript, Node.js/npm
- Maven, Docker Compose, Mailpit
- Springdoc OpenAPI

## Uruchomienie lokalne

Na Linux/macOS wykonaj kroki z sekcji „Demo w kilka minut”. Flyway załaduje
dane demonstracyjne podczas startu backendu. Backend będzie dostępny pod adresem
<http://localhost:8080>, a frontend pod <http://localhost:4200>.

`SPRING_CONFIG_LOCATION=classpath:/` wymusza użycie konfiguracji z zasobów
aplikacji. Na Windows PowerShell:

```powershell
$env:SPRING_CONFIG_LOCATION='classpath:/'
./mvnw.cmd spring-boot:run
```

Dev server przekazuje żądania `/api` i `/internal` do backendu na porcie 8080.
Zależności, cache Angulara i wynik builda są tworzone lokalnie — nie są
przechowywane w Git.

### Rejestracja i aktywacja lokalnie

Po uruchomieniu aplikacji możesz sprawdzić pełny proces rejestracji:

1. Załóż konto w formularzu rejestracji.
2. Otwórz <http://localhost:8025> i znajdź wiadomość aktywacyjną.
3. Kliknij link — frontend automatycznie wyśle żądanie aktywacji do backendu.
4. Po potwierdzeniu wróć do logowania i zaloguj się nowym kontem.

Lokalny Mailpit przechwytuje wiadomości, więc do testu nie potrzeba prawdziwej
skrzynki ani zewnętrznego dostawcy poczty.

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

Konto i hasło demonstracyjne są podane na początku README, w sekcji „Demo w kilka minut”.

Fixture jest idempotentny w tym sensie, że ponowne uruchomienie nie nadpisuje
istniejących ani lokalnie zmodyfikowanych danych Aurora. Dane PostgreSQL są
przechowywane w wolumenie Compose: jeśli zmienisz hasło konta demo albo dane
klubu, ponowny start aplikacji nie przywróci pierwotnego stanu.

## API

Dokumentacja OpenAPI i interaktywny Swagger UI są dostępne po uruchomieniu:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Publiczne są endpointy `/api/auth/**` (w tym aktywacja i reset hasła) oraz
dokumentacja OpenAPI. Pozostałe endpointy wymagają JWT. Przekaż go w nagłówku:

```http
Authorization: Bearer <jwt-token>
```

### Uwierzytelnianie

| Metoda | Endpoint | Działanie |
|---|---|---|
| `POST` | `/api/auth/register` | Tworzy konto oczekujące na aktywację i jego profil; nie zwraca JWT. |
| `POST` | `/api/auth/login` | Uwierzytelnia użytkownika i zwraca token JWT. |
| `POST` | `/api/auth/activate?userExternalId={uuid}&token={token}` | Weryfikuje link aktywacyjny i aktywuje konto. |
| `POST` | `/api/auth/password-reset` | Przyjmuje `{ "email": "..." }`; odpowiada tak samo niezależnie od istnienia konta. |
| `POST` | `/api/auth/password-reset/confirm` | Przyjmuje `{ "token": "...", "newPassword": "..." }`; ustawia nowe hasło bez JWT. |

Nowe konto otrzymuje status oczekujący. Wysyłka wiadomości aktywacyjnej jest
zlecana asynchronicznie przez Kafkę, a hash tokenu z 15-minutowym TTL jest przechowywany
w Redisie. JWT jest wydawany podczas logowania po aktywacji.
Link resetujący jest ważny przez 15 minut; nowa prośba unieważnia poprzedni link
dla danego konta. Po zmianie hasła użytkownik loguje się ponownie.

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
  "scheduledAt": "2030-12-12T16:00:00Z",
  "matchLocation": {
    "city": "Miasto Demo",
    "zipCode": "00-001",
    "street": "Sportowa",
    "buildingNumber": "1"
  },
  "season": 2030
}
```

UUID-y wskazują kluby Aurora Volley i Orion Volley z danych demo. Przed wysłaniem
żądania ustaw `scheduledAt` na datę przyszłą (pole wymaga daty bieżącej lub
późniejszej).

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
