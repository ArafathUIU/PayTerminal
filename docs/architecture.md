# PayTerminal — System Architecture

> A production-inspired Android POS payment **simulator**. We simulate a payment
> processor. We do **not** implement real EMV/card reading, banking, or
> bKash/Nagad integrations — there is no acquirer hardware or infrastructure
> behind this, and the project is positioned honestly as a simulator.

## 1. High-level components

```
┌───────────────────────────────────┐
│        Android POS Terminal       │
│                                   │
│  Java + Android SDK (MVVM)        │
│  Single Activity + Fragments      │
│  Navigation Component             │
│  ViewModel  →  Repository         │
│        ├────────────┬─────────────┤
│        ▼            ▼             │
│  Retrofit/OkHttp   Room           │
│        │            │             │
│        │       Local transaction  │
│        │       mirror + outbox    │
│        ▼                          │
│  HTTPS / REST / JSON              │
└───────────────┬───────────────────┘
                │
                ▼
┌───────────────────────────────────┐
│        Payment Backend            │
│                                   │
│  ASP.NET Core (layered)           │
│  REST API + JWT                   │
│  Payment processing workflow      │
│  State machine enforcement        │
│  Idempotency protection           │
│  Simulation mode (dev)            │
│        │                          │
│        ▼                          │
│  Entity Framework Core            │
│        │                          │
│        ▼                          │
│  PostgreSQL                       │
└───────────────────────────────────┘
```

## 2. Android app architecture (MVVM)

| Layer            | Responsibility                                                      |
| ---------------- | ------------------------------------------------------------------- |
| **UI**           | Activities/Fragments render screens and observe state (LiveData)     |
| **ViewModel**    | Holds UI state, delegates to repository, survives configuration change |
| **Repository**   | Single source of truth for a screen; coordinates remote + local data  |
| **Remote**       | Retrofit/OkHttp API client for the backend REST contract             |
| **Local**        | Room: users, merchants, terminals, and locally-simulated transactions |
| **DI**           | Hilt wires the graph (Retrofit, OkHttp interceptors, Room DAOs, repos) |

### Current implementation status (v2, 19 Aug 2026)

- **Auth, registration, and terminal pairing are real network calls** to the
  backend (JWT, `AuthInterceptor` + `AuthAuthenticator` refresh).
- **Payments and refunds are simulated on-device** by
  `PaymentRepository` (Room only — no network). See
  [current-state.md](current-state.md) for the full Q&A.
- **Role-based access:** the signed-in `UserEntity.role` (`Owner`/`Cashier`)
  gates owner-only features. CASHIER users don't see the Home "Terminal" tab
  or the details "Refund" button; `RefundViewModel` and
  `TerminalManagementFragment` re-check the role as a safety net.

Data flow for a payment (current):

```
PaymentProcessingFragment
      │  amountPaise, method, maskedRef
      ▼
PaymentProcessingViewModel      (computes shouldFail: amountPaise % 100 == 99)
      │
      ▼
PaymentRepository.process(...)  ── simulated "processor" on a background
      │                             thread (sleep ≈ round-trip), then
      ▼                             upsert the transaction
Room → PaymentTransactionDao   ──► LiveData observers re-render UI
```

Data flow for auth / pairing (current — real network):

```
LoginFragment → LoginViewModel → AuthRepository.login()
      ├──► Retrofit AuthApi.login() ──► ASP.NET Core POST /api/v1/auth/login
      └──► TokenStore (EncryptedSharedPreferences) + Room users cache
```

### Local persistence & offline behavior
- Room stores the local session cache (user, merchant, terminal) and every
  transaction/refund created by the payment simulator, so history, receipts,
  and dashboard stats survive restarts and work even while the backend is
  unreachable.
- JWT tokens are kept in Keystore-backed `EncryptedSharedPreferences`; when the
  access token expires, `AuthAuthenticator` refreshes it transparently and
  retries the original request. If refresh fails, the session gate returns the
  user to login.
- Room entities map 1:1 to the database contract (`docs/database-contract.md`).

## 3. Backend architecture (layered)

| Project            | Responsibility                                            |
| ------------------ | --------------------------------------------------------- |
| **PayTerminal.Api**    | HTTP endpoints, DTOs, JWT issuing/validation, middleware |
| **PayTerminal.Application** | Use cases / services: payment processing, refunds, state transitions |
| **PayTerminal.Domain**   | Entities, value objects, enums, state machine rules       |
| **PayTerminal.Infrastructure** | EF Core `DbContext`, repositories, migrations, Postgres |

Layering rule: the Api layer references Application; Application references
Domain; Infrastructure implements the persistence contracts defined in
Application/Domain. No circular dependencies.

## 4. Core design decisions

1. **Transaction + PaymentAttempts + TransactionEvents.** A `Transaction` is
   the logical order identified by an idempotency key. Each retry is a new
   `PaymentAttempt`. Every state transition is recorded in `TransactionEvents`
   for auditing and debugging.
2. **Server-driven simulation.** The backend decides payment outcomes via
   config or the `X-Simulation-Mode` header (works from `curl`). "Network
   timeout" is simulated server-side by delaying the response beyond the
   client's timeout — reproducing a genuine client-side timeout.
3. **Idempotency.** The client sends `Idempotency-Key` on payment creation. The
   backend enforces a unique key so repeated requests return the original
   transaction instead of charging twice.
4. **JWT auth.** Merchants register; staff users log in to obtain a JWT carrying
   user + merchant scope. Terminals pair to a merchant via a pairing code.
5. **Currency.** All amounts are `decimal`, ISO 4217 code (default `BDT`).
   Never floating point.

## 5. Key flows

### New payment
1. Merchant enters amount, selects method (CARD / QR / E_WALLET), confirms.
2. `PaymentProcessingViewModel` applies the demo decline rule
   (amount ending in `.99` → `FAILED`) and calls
   `PaymentRepository.process(...)`.
3. The on-device simulator writes the transaction to Room (`SUCCESS` or
   `FAILED`) after a short delay that mimics a processor round-trip.
4. The app renders success/failure and offers retry for failures.

### Refund
1. Merchant opens a `SUCCESS` transaction and requests a refund.
2. `PaymentRepository.refund(...)` simulates the refund processor and marks
   the transaction `REFUNDED` (with `refundedAt` + `refundReason`).

> The richer server-driven flows described below (idempotency, attempts,
> events, `POST /api/v1/payments`, `POST /api/v1/refunds`) are the target
> design; the corresponding domain code exists in `backend/PayTerminal.Domain`
> but is **not yet wired to controllers**.

### Terminal health
- The terminal posts a heartbeat (`POST /api/v1/terminals/{id}/heartbeat`) on a
  schedule. The backend flags the terminal offline when heartbeats stop.

## 6. Environment & tooling

- Android: Java 11, minSdk 24, targetSdk 37, Gradle, AGP
- Backend: .NET 8 (ASP.NET Core), EF Core, Npgsql, JWT
- Database: PostgreSQL 16 via Docker Compose (see `docker-compose.yml`)
- Auth: JWT bearer tokens
