# PayTerminal — State Machines

Three state machines live in `PayTerminal.Domain`. Transitions are enforced by
the domain (a transition not listed below is rejected). Every transition is
appended to `TransactionEvents`.

## 1. Transaction

Statuses: `INITIATED`, `PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`, `CANCELLED`

```
                 (retry)
        ┌──────────────┐
        ▼              │
INITIATED → PENDING → PROCESSING ──► SUCCESS
                  │          │
                  │          └──────► FAILED
                  ▼
              CANCELLED
```

| From       | To         | Trigger                                  |
| ---------- | ---------- | ---------------------------------------- |
| INITIATED  | PENDING    | server accepted the order                |
| PENDING    | PROCESSING | processor call started                   |
| PROCESSING | SUCCESS    | processor approved (records attempt)     |
| PROCESSING | FAILED     | processor declined / error (records attempt) |
| PENDING    | CANCELLED  | merchant cancelled before processing     |
| FAILED     | PENDING    | retry with same idempotency key (new attempt) |

Rules
- Terminal states: `SUCCESS`, `FAILED`, `CANCELLED`.
- Retry only allowed from `FAILED`, and only with the same `Idempotency-Key`
  (→ the same transaction row; a new `PaymentAttempt` is created).
- No transition ever leaves `SUCCESS` (refunds are tracked separately, not as
  a transaction status).

## 2. PaymentAttempt

Statuses: `PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`

```
PENDING → PROCESSING → SUCCESS
              │
              └──────→ FAILED
```

- Attempt number increments per retry on the same transaction.
- `FAILED` attempts record `error_code` + `error_message`.
- On retry, the previous attempt stays `FAILED`; a fresh attempt is created.

## 3. Refund

Statuses: `REQUESTED`, `PROCESSING`, `COMPLETED`, `REJECTED`

```
REQUESTED → PROCESSING → COMPLETED
               │
               └──────→ REJECTED
```

| From      | To         | Trigger                                        |
| --------- | ---------- | ---------------------------------------------- |
| REQUESTED | PROCESSING | refund processor call started                  |
| PROCESSING| COMPLETED  | refund approved                                |
| PROCESSING| REJECTED   | refund declined (reason recorded)              |

On `COMPLETED`, the parent transaction is marked with `refunded_at` +
`refunded_amount` (it does **not** change `status`).

## 4. Example timelines

### Successful payment
```
INITIATED → PENDING → PROCESSING → SUCCESS
```
Events: `TRANSITION(INITIATED→PENDING)`, `TRANSITION(PENDING→PROCESSING)`,
`TRANSITION(PROCESSING→SUCCESS)`.

### Failed then retried (network drop / declined)
```
INITIATED → PENDING → PROCESSING → FAILED      (attempt 1, FAILED)
              │ (retry, same idempotency key)
              ▼
           PENDING → PROCESSING → SUCCESS      (attempt 2, SUCCESS)
```

### Merchant cancels
```
INITIATED → PENDING → CANCELLED
```

### Refund
```
SUCCESS (refund requested)
  └─► REQUESTED → PROCESSING → COMPLETED   ⇒ transaction.refunded_at = now
```
