# PayTerminal

A production-inspired **Android POS payment terminal simulator**. The Android
app acts as a merchant's POS terminal; an ASP.NET Core backend simulates the
payment-processing infrastructure. Together they demonstrate realistic payment
workflows, transaction state management, error handling, retry, refunds, and
local persistence.

> **What this is:** a simulator. The backend *simulates* a payment processor —
> there is no real card reading (EMV/NFC), no banking integration, and no
> bKash/Nagad/acquire integration. The value is in faithfully modeling the
> client–server payment flow, state machines, idempotency, and resilience.

## How a payment works

1. The merchant enters an amount (e.g. ৳1,250) and picks a method
   (Card / QR / E-Wallet).
2. The Android terminal calls `POST /api/v1/payments` with an
   `Idempotency-Key`.
3. The backend runs the transaction through a state machine
   (`INITIATED → PENDING → PROCESSING → SUCCESS | FAILED`) and simulates the
   processor outcome.
4. The terminal renders success/failure, offers retry on failure, and stores
   the transaction locally in Room.
5. Later: history, search, receipts, and refunds (`REQUESTED → PROCESSING →
   COMPLETED | REJECTED`).

Design details live in [`docs/`](docs/):

- [`docs/architecture.md`](docs/architecture.md) — component + app/backend layering
- [`docs/database-contract.md`](docs/database-contract.md) — schema, enums, idempotency
- [`docs/api-contract.md`](docs/api-contract.md) — REST endpoints, headers, errors
- [`docs/state-machine.md`](docs/state-machine.md) — transaction/attempt/refund flows

## Repository layout

```
app/                    Android POS terminal (Java, MVVM, Retrofit, Room, Hilt)
backend/                ASP.NET Core payment backend (.NET 8, EF Core, PostgreSQL)
docs/                   Architecture, API, database, state-machine contracts
docker-compose.yml      Local PostgreSQL 16
```

## Tech stack

| Layer    | Technology |
| -------- | ---------- |
| Android  | Java, Android SDK, MVVM, Retrofit, OkHttp, Room, Hilt |
| Backend  | ASP.NET Core, Entity Framework Core, JWT |
| Database | PostgreSQL 16 (Docker) |
| Dev ops  | Docker, Git/GitHub |

## Getting started

### 1. Start PostgreSQL

```bash
cp .env.example .env   # adjust credentials if needed
docker compose up -d
```

### 2. Run the backend

```bash
cd backend
dotnet run --project src/PayTerminal.Api
# health check: http://localhost:5058/api/v1/health
```

### 3. Build the Android app

```bash
./gradlew build
```

The emulator reaches the local backend at `http://10.0.2.2:5058`.

## Roadmap

- [x] **Phase 1 — Foundation:** design docs, backend skeleton, Docker Postgres
- [ ] **Phase 2 — Backend core:** domain models, EF Core, JWT auth, merchant/terminal registration
- [ ] **Phase 3 — Android core:** MVVM, Retrofit/OkHttp, Room, Hilt, login + registration
- [ ] **Phase 4 — Payment domain:** dashboard, amount entry, card/QR/e-wallet screens
- [ ] **Phase 5 — Payment processing:** state machine, idempotency, simulation modes
- [ ] **Phase 6 — Reliability:** local storage, retry, network-failure handling
- [ ] **Phase 7 — History & ops:** history, search/filter, receipts, refunds
- [ ] **Phase 8 — Polish:** health monitoring, API docs, tests, final polish
