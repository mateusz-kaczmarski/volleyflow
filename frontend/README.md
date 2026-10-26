# VolleyFlow — frontend demonstracyjny

Interfejs Angular do lokalnego uruchomienia i demonstracji funkcji backendu
VolleyFlow. Frontend powstał przy wsparciu narzędzi AI; głównym obszarem projektu
jest backend Java/Spring Boot.

## Requirements

- Node.js 20.19+ or 22.12+
- Backend running at `http://localhost:8080`

## Run locally

```bash
npm ci
npm start
```

Przed uruchomieniem frontendu uruchom infrastrukturę Docker Compose i backend
zgodnie z głównym README. Dev server jest dostępny pod `http://localhost:4200`
i przekazuje żądania `/api` oraz `/internal` do backendu na porcie 8080.

Do obejrzenia aplikacji możesz zalogować się kontem demonstracyjnym opisanym w
głównym README. Proces rejestracji można przejść lokalnie z Mailpitem pod
`http://localhost:8025`.

## Match statistics API contract

The match sheet uses the existing match and set endpoints for scores. For each
real set it expects the following endpoints (authenticated and authorized for
the club and match):

- `GET /internal/matches/{clubExternalId}/{matchExternalId}/set/{setExternalId}/statistics`
  returns `{ "players": [] }` if there are no saved statistics yet.
- `PUT /internal/matches/{clubExternalId}/{matchExternalId}/set/{setExternalId}/statistics`
  replaces the **entire** set sheet with the JSON body described below; a 2xx
  response (including 204) confirms persistence. An empty `players` array
  clears all rows in that set.
- `DELETE /internal/matches/{clubExternalId}/{matchExternalId}/set/{setExternalId}`
  deletes the set, returning 204. The frontend removes its local draft only
  after a successful response.

The GET response and PUT request have the same shape. Each `playerExternalId`
is the player's **person profile external ID** (`personProfileExternalId` from
`GET /api/memberships/{clubExternalId}/players`), not a membership ID. All
statistic fields are numbers; names and positions are resolved from the club
roster on the frontend and are not sent to the statistics API:

```json
{
  "players": [{
    "playerExternalId": "player-profile-uuid",
    "points": 1, "balance": 1,
    "serveTotal": 1, "serveError": 0, "serveAces": 0,
    "receptionTotal": 1, "receptionError": 0,
    "receptionPositivePercent": 100, "receptionPerfectPercent": 0,
    "attackTotal": 1, "attackError": 0, "attackBlocked": 0, "attackPoints": 1,
    "blockPoints": 0, "blockTouches": 0, "defense": 0, "assists": 0
  }]
}
```

Editing keeps a per-account/per-match/per-set browser draft. The Save button
sends modified sets to the statistics API; the UI reports success only after a
successful server response.

## Build

```bash
npm run build
```
