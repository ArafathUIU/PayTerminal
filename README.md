# PayTerminal

A production-inspired **Android POS payment terminal simulator**. The Android
app acts as a merchant's POS terminal; an ASP.NET Core backend provides real
JWT auth, merchant registration, and terminal pairing. Payment and refund
processing is **simulated locally on the device** and persisted in Room.

> **What this is:** a simulator. There is no real card reading (EMV/NFC), no
> banking or bKash/Nagad/acquire integration, and the payment processor is
> simulated on-device. The value is in the production-shaped architecture:
> MVVM, Hilt, Room, Retrofit/OkHttp with token refresh, a session state
> machine, transaction history, receipts, and refunds.

## How a payment works

1. The merchant signs in (real JWT login) and pairs a terminal (real API).
2. The merchant enters an amount (e.g. ৳1,250) and picks a method
   (Card / QR / E-Wallet).
3. `PaymentRepository.process(...)` **simulates the processor locally**
   (amounts ending in `.99` decline, ~1.4 s round-trip) and writes the
   transaction to Room.
4. The terminal renders success/failure and offers retry on failure.
5. Later: history, search/filter, digital receipts, and refunds — refunds are
   also simulated locally (the transaction moves `SUCCESS → REFUNDED`).

Design details live in [`docs/`](docs/):

- [`docs/current-state.md`](docs/current-state.md) — **what is actually implemented today** (architecture Q&A)
- [`docs/architecture.md`](docs/architecture.md) — component + app/backend layering
- [`docs/database-contract.md`](docs/database-contract.md) — schema, enums, idempotency
- [`docs/api-contract.md`](docs/api-contract.md) — REST endpoints, headers, errors
- [`docs/state-machine.md`](docs/state-machine.md) — transaction/attempt/refund flows

## Repository layout

```
app/                    Android POS terminal (Java, MVVM, Retrofit, Room, Hilt)
backend/                ASP.NET Core backend (.NET 8, EF Core, PostgreSQL)
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

Current status — the Android app and the auth/pairing backend are complete;
payments/refunds run locally on-device.

- [x] **Phase 1 — Foundation:** design docs, backend skeleton, Docker Postgres
- [x] **Phase 2 — Backend core:** domain models, EF Core, JWT auth, merchant/terminal registration
- [x] **Phase 3 — Android core:** MVVM, Retrofit/OkHttp, Room, Hilt, login + registration
- [x] **Phase 4 — Payment domain (on-device):** dashboard, amount entry, card/QR/e-wallet screens, local simulation
- [x] **Phase 5 — Payment processing (on-device):** success/failure, retry, simulated decline rule
- [x] **Phase 6 — Reliability:** local persistence, session gate, token refresh, network-failure handling
- [x] **Phase 7 — History & ops:** history, search/filter, receipts, refunds
- [ ] **Phase 8 — Polish:** health monitoring, API docs, tests, final polish

> **Next milestone candidates:** move payment/refund processing to the backend
> (`POST /api/v1/payments`, `POST /api/v1/refunds`) using the domain state
> machines that already exist in `backend/PayTerminal.Domain`; cache the
> merchant profile on login so receipts/details show the business name.
