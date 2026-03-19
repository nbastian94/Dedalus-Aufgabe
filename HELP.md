# Dedalus Aufgabe - Runbook

## Backend (Spring Boot)

Start:

```bash
./mvnw spring-boot:run
```

Tests:

```bash
./mvnw test
```

Wichtige Endpunkte:

- `POST /calculate`
- `GET /calculations`
- `POST /calculations`
- `GET /denominations`

## Frontend (Angular + PrimeNG)

Install:

```bash
cd frontend
npm install
```

Start:

```bash
npm start
```

E2E-Tests (Playwright):

```bash
npx playwright install
npm run e2e
```

## Lokale URLs

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
