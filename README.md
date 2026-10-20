# CafeCrowded

A side project: check where nearby cafes are, and whether they're busy right now, before you head out. I'm building it mostly to get hands-on with React, PostgreSQL and AI-assisted development.

It's still a work in progress, not a finished service.

## What works

- Cafe locations on a map (OpenStreetMap)
- Search by name or address
- Users can report a cafe as quiet, normal or crowded. Only reports from the last 60 minutes count toward the current crowd level.
- No recent reports? The app says "no info" instead of guessing.

## What doesn't yet

- Filling the database with real cafes from the Kakao Local API. The database is empty for now.
- Area-level crowd data from Seoul's real-time city data API
- CI and deployment

I don't want to fake crowd numbers. If nobody has reported, the app shows that it doesn't know. Area-level crowd and per-cafe crowd will be kept as separate values.

## Stack

- Backend: Java 22, Spring Boot 4, Maven, Flyway
- Database: PostgreSQL 16 (Docker Compose, reachable from localhost only)
- Frontend: React, TypeScript, Vite, Leaflet

## Tests

Backend tests use Testcontainers, so Docker needs to be running.

```bash
cd backend && ./mvnw test
cd frontend && npm test
```

## Security notes

Secrets live in environment variables, never in the code. External APIs are called from the backend only. Reporters' IP addresses aren't stored, only a salted hash, and the report endpoint is rate limited.
