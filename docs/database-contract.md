# PayTerminal — Database Contract

PostgreSQL 16. All money is stored as `decimal(18,2)` (never floating point).
Timestamps are `timestamptz` (UTC). Primary keys are UUIDs generated
server-side.

> **Implementation status (19 Aug 2026):** `merchants`, `users`, `terminals`,
> and `refresh_tokens` are actively used (auth, registration, pairing).
> `transactions`, `payment_attempts`, `refunds`, and `transaction_events` are
> created by the migrations but **not yet written** by any controller — the
> Android app currently simulates payments/refunds in Room instead. The schema
> below remains the target contract for when the payment endpoints land.
> Note: in the live DB these tables are lowercase snake_case with
> PascalCase-quoted columns (e.g. `terminals."PairingCode"`).

## 1. Tables

### Merchants
The shop owner / business entity.

| Column      | Type          | Notes                        |
| ----------- | ------------- | ---------------------------- |
| id          | uuid          | PK                           |
| name        | varchar(120)  | Business/owner name          |
| email       | varchar(255)  | unique                       |
| phone       | varchar(20)   | nullable                     |
| created_at  | timestamptz   | default now()                |
| updated_at  | timestamptz   | default now()                |

### Users
Staff login accounts. A user belongs to one merchant.

| Column        | Type         | Notes                    |
| ------------- | ------------ | ------------------------ |
| id            | uuid         | PK                       |
| merchant_id   | uuid         | FK → merchants.id        |
| name          | varchar(120) |                          |
| email         | varchar(255) | unique                   |
| password_hash | varchar(255) | BCrypt / ASP.NET hash    |
| role          | varchar(20)  | OWNER / CASHIER          |
| active        | boolean      | default true             |
| created_at    | timestamptz  | default now()            |

### Terminals
Registered POS devices bound to a merchant.

| Column            | Type         | Notes                          |
| ----------------- | ------------ | ------------------------------ |
| id                | uuid         | PK                             |
| merchant_id       | uuid         | FK → merchants.id              |
| code              | varchar(20)  | unique, e.g. `TERM-0001`       |
| pairing_code      | varchar(10)  | nullable, one-time pair secret |
| status            | varchar(20)  | PAIRED / ACTIVE / OFFLINE      |
| paired_at         | timestamptz  | nullable                       |
| last_heartbeat_at | timestamptz  | nullable                       |
| created_at        | timestamptz  | default now()                  |

### Transactions
The logical payment order. Identified end-to-end by the client-generated
idempotency key; a retry reuses the same transaction.

| Column            | Type          | Notes                              |
| ----------------- | ------------- | ---------------------------------- |
| id                | uuid          | PK                                 |
| transaction_number| varchar(40)   | unique, e.g. `TXN-20260818-00021`  |
| idempotency_key   | varchar(64)   | unique index — see §3              |
| merchant_id       | uuid          | FK → merchants.id                  |
| terminal_id       | uuid          | FK → terminals.id                  |
| user_id           | uuid          | FK → users.id (cashier)            |
| amount            | decimal(18,2) |                                    |
| currency          | char(3)       | ISO 4217, default `BDT`            |
| payment_method    | varchar(20)   | CARD / QR / E_WALLET               |
| status            | varchar(20)   | TransactionStatus enum             |
| created_at        | timestamptz   | default now()                      |
| updated_at        | timestamptz   | default now()                      |
| processed_at      | timestamptz   | nullable                           |
| refunded_at       | timestamptz   | nullable                           |
| refunded_amount   | decimal(18,2) | nullable                           |

### PaymentAttempts
One per processor interaction. Retries create new attempt rows against the
same transaction.

| Column             | Type          | Notes                              |
| ------------------ | ------------- | ---------------------------------- |
| id                 | uuid          | PK                                 |
| transaction_id     | uuid          | FK → transactions.id               |
| attempt_number     | int           | 1-based per transaction            |
| method             | varchar(20)   | snapshot of payment_method         |
| masked_reference   | varchar(40)   | e.g. card `•••• 4821`              |
| processor_reference| varchar(64)   | simulated processor ref            |
| status             | varchar(20)   | AttemptStatus enum                 |
| error_code         | varchar(40)   | nullable                           |
| error_message      | varchar(255)  | nullable                           |
| created_at         | timestamptz   | default now()                      |
| processed_at       | timestamptz   | nullable                           |

### Refunds
One refund per refund request. Refunding a partially refunded transaction is
allowed up to the paid amount.

| Column         | Type          | Notes                              |
| -------------- | ------------- | ---------------------------------- |
| id             | uuid          | PK                                 |
| refund_number  | varchar(40)   | unique, e.g. `RFD-20260818-00003`  |
| transaction_id | uuid          | FK → transactions.id               |
| amount         | decimal(18,2) | <= transaction amount              |
| reason         | varchar(255)  | nullable                           |
| status         | varchar(20)   | RefundStatus enum                  |
| created_at     | timestamptz   | default now()                      |
| processed_at   | timestamptz   | nullable                           |

### RefreshTokens
Issued on login, rotated on refresh, revoked when a new token supersedes them
(one active token per user).

| Column         | Type         | Notes                        |
| -------------- | ------------ | ---------------------------- |
| id             | uuid         | PK                           |
| user_id        | uuid         | FK → users.id                |
| token          | varchar(64)  | unique, random value         |
| expires_at     | timestamptz  |                              |
| revoked        | boolean      | default false                |
| created_at     | timestamptz  | default now()                |
| revoked_at     | timestamptz  | nullable                     |

### TransactionEvents
Immutable audit log of every state transition (and other notable events).

| Column       | Type          | Notes                                  |
| ------------ | ------------- | -------------------------------------- |
| id           | uuid          | PK                                     |
| transaction_id | uuid        | FK → transactions.id                   |
| event_type   | varchar(40)   | e.g. `TRANSITION`, `RETRY`, `REFUND`   |
| from_status  | varchar(20)   | nullable                               |
| to_status    | varchar(20)   | nullable                               |
| payload      | jsonb         | extra context, nullable                |
| created_at   | timestamptz   | default now()                          |

## 2. Enums

| Enum              | Values                                        |
| ----------------- | --------------------------------------------- |
| `PaymentMethod`   | `CARD`, `QR`, `E_WALLET`                      |
| `TransactionStatus` | `INITIATED`, `PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`, `CANCELLED` |
| `AttemptStatus`   | `PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`  |
| `RefundStatus`    | `REQUESTED`, `PROCESSING`, `COMPLETED`, `REJECTED` |
| `TerminalStatus`  | `PAIRED`, `ACTIVE`, `OFFLINE`                 |
| `UserRole`        | `OWNER`, `CASHIER`                            |

> Note: `REFUNDED` is **not** a transaction status. A transaction whose refund
> completed is marked via `refunded_at` + `refunded_amount`. This keeps the
> transaction state machine clean (see `state-machine.md`).

## 3. Idempotency

- The client generates a UUID per logical payment order and sends it in the
  `Idempotency-Key` header.
- `transactions.idempotency_key` has a **unique index**.
- If a request arrives with an existing key, the backend returns the existing
  transaction (same response as the original success) instead of creating a new
  one. This guarantees no double charge when the app retries after a network
  drop.

## 4. Indexes & constraints

- `transactions(idempotency_key)` — **unique**
- `transactions(merchant_id, created_at desc)` — history browsing per merchant
- `transactions(terminal_id, created_at desc)` — per-terminal history
- `payment_attempts(transaction_id, attempt_number)` — unique per transaction
- `refunds(transaction_id)` — find refunds for a transaction
- `transaction_events(transaction_id, created_at)` — event timeline
- `refresh_tokens(user_id, revoked)` — active token lookup
- `refresh_tokens(token)` — **unique**

## 5. Relationships

```
Merchants 1──n Users
Merchants 1──n Terminals
Merchants 1──n Transactions
Terminals 1──n Transactions
Users    1──n Transactions
Users    1──n RefreshTokens
Transactions 1──n PaymentAttempts
Transactions 1──n TransactionEvents
Transactions 1──n Refunds
```
