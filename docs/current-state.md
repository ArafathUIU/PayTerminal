# PayTerminal — Current State (Architecture Q&A)

Answers to the recurring architecture questions, as of **19 Aug 2026**. This
document reflects what is **actually implemented**, not the target design.
Where the code deviates from earlier docs, this file wins.

---

## 1. How is authentication currently implemented?

Real JWT auth against the ASP.NET Core backend — this is one of the few
*genuinely networked* parts of the app.

Flow:

```
LoginFragment → LoginViewModel → AuthRepository.login()
    → Retrofit AuthApi.login() → POST /api/v1/auth/login   (real HTTP)
    → on success: persistLogin()
         ├── TokenStore.saveTokens(access, refresh, expiresAt)
         │     EncryptedSharedPreferences (Keystore AES256_GCM / AES256_SIV)
         └── UserDao.upsert(UserEntity)                    (Room cache)
```

- **Requests** carry `Authorization: Bearer <access>` injected by
  `AuthInterceptor`.
- **401 handling** (`AuthAuthenticator`): synchronized single-flight refresh via
  an `@Authless` `AuthApi` (a second OkHttp client without the bearer
  interceptor/authenticator, so a revoked refresh token can't loop). On success
  the original request is retried; on failure tokens are cleared.
- **Session gate:** `SessionViewModel` derives a state machine
  (`LOADING → LOGGED_OUT | NEEDS_TERMINAL | READY`) from three observables —
  Room `UserEntity`, Room `TerminalEntity`, and `TokenStore.sessionActive()` —
  and `MainActivity` routes navigation accordingly.
- **Register:** `POST /api/v1/merchants/register` then auto-login.
- **Logout:** clears tokens and deletes Room users/merchants/terminals.
- Tokens deliberately **never** live in Room; they live in
  Keystore-backed `EncryptedSharedPreferences` (`payterminal_tokens`).

## 2. Where are Merchant, User and Terminal represented?

Every layer has its own shape:

| Concept | Postgres (backend) | DTO (Retrofit) | Room entity | UI |
| --- | --- | --- | --- | --- |
| **Merchant** | `merchants` table | `MerchantRegistrationResponse` | `MerchantEntity` (`merchants`) | Register, Settings |
| **User** | `users` table | `UserDto` (in `AuthResponse`) | `UserEntity` (`users`) | Login, Home, Settings |
| **Terminal** | `terminals` table | `TerminalResponse` | `TerminalEntity` (`terminals`) | Pair, Home, Terminal Management |

- Backend is the source of truth (Postgres, EF Core). Columns are
  PascalCase-quoted in Postgres (`"PairingCode"`, `"Status"`, …).
- On Android they are **local caches** written on login/register/pair:
  - `AuthRepository` → `UserDao` / `MerchantDao`
  - `TerminalRepository.pair()` → `TerminalApi.register()` then
    `TerminalDao.upsert()`
- Merchant name shown on receipt/details/terminal screens currently falls back
  to a placeholder because `MerchantEntity` is only persisted during
  registration, not login (known cosmetic gap).

## 3. What Room entities currently exist?

Database: `payterminal.db`, **version 2**, `fallbackToDestructiveMigration()`,
`exportSchema = false`. No `@TypeConverters` — all columns are `String` /
`long` / `int`.

| Entity | Table | Key fields |
| --- | --- | --- |
| `UserEntity` | `users` | `id` (PK), `name`, `email`, `role`, `merchantId` |
| `MerchantEntity` | `merchants` | `merchantId` (PK), `name`, `businessName`, `email`, `phone` |
| `TerminalEntity` | `terminals` | `id` (PK), `merchantId`, `code`, `name`, `status`, `pairedAt`, `lastHeartbeatAt` |
| `PaymentTransactionEntity` | `payment_transactions` | `id` (PK), `merchantId`, `terminalId`, `terminalCode`, `amount` (long, minor units), `currency`, `method`, `status`, `reference`, `cardMasked`, `createdAt`, `processedAt`, `refundedAt`, `refundReason` |

DAOs: `UserDao`, `MerchantDao`, `TerminalDao`, `PaymentTransactionDao`. The
transaction DAO exposes LiveData aggregate queries used by the dashboard
(sum of SUCCESS, counts by status, refunded total, recent N, all for merchant).

## 4. Where is payment logic located?

- **`data/repository/PaymentRepository.java`** — `process()` and `refund()`.
  The only class in the data layer that "runs" a payment; injected dependency
  is solely `PaymentTransactionDao`.
- **`ui/payment/PaymentProcessingViewModel.java`** — computes `shouldFail`
  and invokes `paymentRepository.process(...)`.
- **UI chain:** `NewPaymentFragment` (amount keypad) → `PaymentMethodFragment`
  → `CardPaymentFragment` / `QrPaymentFragment` / `WalletPaymentFragment`
  (cosmetic credential capture) → `PaymentProcessingFragment` → `PaymentSuccess
  Fragment` / `PaymentFailedFragment`.
- **`ui/refund/RefundViewModel` + `PaymentRepository.refund()`** — refunds.
- **Stats/history** are Room LiveData queries, not payment logic per se.

## 5. Is payment currently simulated locally or through an API?

**Locally, entirely.** Payment and refund never touch the network:

```java
// PaymentRepository — no Retrofit dependency, no Call, only the DAO
new Thread(() -> {
    ... build PaymentTransactionEntity (TXN-<uuid>, REF-<uuid>, currency "BDT")
    tx.status = shouldFail ? FAILED : SUCCESS;
    sleep(1400);              // simulated processor round-trip
    dao.upsert(tx);
    onResult.accept(tx);
}, "payment-simulator").start();
```

- Decline rule (in `PaymentProcessingViewModel`):
  `boolean shouldFail = amountPaise % 100 == 99; // amounts ending in .99`
- Refund: `sleep(1000)`, then `status = REFUNDED`, `refundedAt = now`,
  `refundReason = reason`, `dao.update(...)`.
- Only **auth** (login/register), **terminal pairing**, and **token refresh**
  are real HTTP calls. Everything about a payment is simulated on-device and
  persisted in Room.

## 6. How are transaction states represented?

A single `String` column (`payment_transactions.status`) with three constants
on `PaymentTransactionEntity`:

```
SUCCESS   → terminal payment approved
FAILED    → declined by the demo rule
REFUNDED  → a refund completed (same row mutated: status, refundedAt, refundReason)
```

There is **no** multi-table state machine on the device (no
attempts/events/refunds tables in Room) — a transaction is one row that moves
`SUCCESS → REFUNDED`. Methods are `CARD` / `QR` / `WALLET`; currency is always
`"BDT"`. Amounts are stored as integer **minor units** (`amountPaise`, i.e. the
same value the keypad builds, e.g. ৳299 → 299).

The richer backend state machine (`INITIATED → PENDING → PROCESSING →
SUCCESS|FAILED`, attempts, events) still exists in `backend/PayTerminal.Domain`
but is **not wired to any controller** — see §7 and `docs/api-contract.md`.

## 7. What does the current project architecture look like?

```
┌────────────────────────────────────────────────────────────┐
│ Android app (single module, pure Java, package ...payterminalversion2) │
│                                                            │
│  MainActivity (single activity) ── SessionViewModel gate   │
│        │  Navigation Component (21 fragment destinations)   │
│        ▼                                                    │
│  Fragment → ViewModel (@HiltViewModel) → Repository         │
│                                  │                          │
│                          ┌───────┴────────┐                 │
│                          ▼                ▼                 │
│                    Retrofit/OkHttp    Room (payterminal.db) │
│                    (auth, pair only)  (users, merchants,    │
│                                       terminals, payments)  │
└───────────────┬────────────────────────────────────────────┘
                │  HTTP (login / register / pair / refresh)
                ▼
┌────────────────────────────────────────────────────────────┐
│ ASP.NET Core backend (backend/)                            │
│  PayTerminal.Api      → Auth, Merchants, Terminals controllers │
│  PayTerminal.Application → AuthService, TerminalService     │
│  PayTerminal.Domain   → entities + state machines (unused)  │
│  PayTerminal.Infrastructure → EF Core → PostgreSQL 16       │
└────────────────────────────────────────────────────────────┘
```

- **Android layering:** MVVM. `data/` = `local` (Room DAOs/entities/db),
  `remote` (Retrofit APIs, DTOs, interceptors), `repository` (coordination),
  `session` (token vault); `di/` = Hilt modules; `ui/` = one package per
  feature with Fragment + ViewModel.
- **DI (Hilt):** `NetworkModule` (Retrofit/OkHttp/Gson/APIs), `DatabaseModule`
  (Room + DAOs), `AppModule` (single-thread `@IoExecutor`); qualifiers
  `@Authless` and `@IoExecutor`.
- **Backend layering:** Api → Application → Domain, with Infrastructure
  implementing persistence. Currently only **Auth, Merchants, Terminals** are
  exposed; the payment domain (`Transaction`, `PaymentAttempt`, `Refund`,
  `TransactionEvent`, state machines) exists in code but has **no
  controllers/services** — those endpoints were intentionally not ported.

## 8. Are Retrofit/OkHttp already configured?

Yes, fully wired in `di/NetworkModule.java`:

- **Base URL:** `BuildConfig.API_BASE_URL` = `"http://10.0.2.2:5058/"`
  (emulator → host ASP.NET Core dev backend; cleartext allowed for
  `10.0.2.2`/`localhost` via `network_security_config.xml`).
- **Converter:** `GsonConverterFactory` on both Retrofit instances.
- **OkHttp client 1 (authenticated):** `AuthInterceptor` (injects Bearer),
  `AuthAuthenticator` (401 → refresh → retry), timeouts 15/30/30 s,
  `HttpLoggingInterceptor(BASIC)` in debug only.
- **OkHttp client 2 (`@Authless`):** no interceptor/authenticator; used solely
  for the refresh call to avoid refresh-token re-entry loops.
- **API interfaces:** `AuthApi` (`/auth/login`, `/auth/refresh`),
  `MerchantApi` (`/merchants/register`), `TerminalApi`
  (`/terminals/register`, `/terminals/{id}`, `/terminals/{id}/heartbeat`).

## 9. What is currently mock data versus real persistence?

**Simulated (on-device "processor"):**
- Payment outcome — decline rule `amountPaise % 100 == 99`.
- Latency — `Thread.sleep(1400)` (process), `Thread.sleep(1000)` (refund).
- Identifiers — `TXN-<uuid10>`, `REF-<uuid8>` generated locally.
- Credentials capture — card/QR/wallet inputs are cosmetic only
  (`"•••• •••• •••• ••••"`, `"QR-CAPTURED"`, `"****" + last4`).
- Currency — hardcoded `"BDT"` / `"৳"` in `Money.format`.

**Real persistence:**
- Room `payterminal.db` stores users, merchants, terminals, and every
  transaction/refund — survives restarts and app re-installs (`-r`).
- JWT tokens in Keystore-backed `EncryptedSharedPreferences`.
- Backend PostgreSQL persists merchants, users, terminals (seeded by
  `DbSeeder`: **Rahim Electronics**, owner **owner@myshop.com / password123**,
  terminal **TERM-0001 / pairing code ABC-1234**).
- Auth, registration, and pairing are real network round-trips.

**No seed/prefill data exists inside the app itself** — every row comes from a
live login/register/pair/process/refund.

---

*Related docs: [architecture](architecture.md), [api-contract](api-contract.md),
[database-contract](database-contract.md), [state-machine](state-machine.md),
[technology-decisions](technology-decisions.md).*