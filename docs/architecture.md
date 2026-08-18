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
| **Local**        | Room: mirrors backend entities, queues offline actions in an outbox  |
| **DI**           | Hilt wires the graph (Retrofit, OkHttp interceptors, Room DAOs, repos) |

Data flow for a payment:

```
PaymentFragment
      │
      ▼
PaymentViewModel
      │
      ▼
PaymentRepository
      ├─────────────► PaymentRemoteDataSource ──► Retrofit ──► ASP.NET Core
      │                     ▲  on success: sync server response
      └─────────────► Room (TransactionDao)  ◄──┘    to local DB
                     outbox row written before network call
```

### Local persistence & offline behavior
- Room stores a local mirror of transactions so the merchant can browse
  history even when the backend is unreachable.
- The **outbox** table queues outgoing actions (create payment, heartbeat)
  when offline. On reconnect, queued actions are retried with their original
  `Idempotency-Key` so no duplicate charge can occur.
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
2. App writes an outbox row, generates `Idempotency-Key`, calls
   `POST /api/v1/payments`.
3. Backend validates, persists `INITIATED → PENDING → PROCESSING`, calls the
   simulated processor, and moves the transaction to `SUCCESS` or `FAILED`.
4. App updates local Room state and renders success/failure, with retry for
   failures.

### Refund
1. Merchant opens a `SUCCESS` transaction and requests a refund.
2. `POST /api/v1/refunds` creates a refund: `REQUESTED → PROCESSING →
   COMPLETED | REJECTED`. On completion the transaction is marked refunded.

### Terminal health
- The terminal posts a heartbeat (`POST /api/v1/terminals/{id}/heartbeat`) on a
  schedule. The backend flags the terminal offline when heartbeats stop.

## 6. Environment & tooling

- Android: Java 11, minSdk 24, targetSdk 37, Gradle, AGP
- Backend: .NET 8 (ASP.NET Core), EF Core, Npgsql, JWT
- Database: PostgreSQL 16 via Docker Compose (see `docker-compose.yml`)
- Auth: JWT bearer tokens
