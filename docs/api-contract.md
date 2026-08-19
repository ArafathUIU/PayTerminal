# PayTerminal — API Contract

Base URL: `http://localhost:5058` locally (the Android emulator reaches the
host via `http://10.0.2.2:5058`, see `BuildConfig.API_BASE_URL`). All
responses are JSON. Protected endpoints require `Authorization: Bearer <jwt>`.

## Implementation status (19 Aug 2026)

| Resource | Endpoints | Status |
| --- | --- | --- |
| Auth | `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh` | ✅ implemented |
| Merchants | `POST /api/v1/merchants/register` | ✅ implemented |
| Terminals | `POST /api/v1/terminals/register`, `GET /api/v1/terminals/{id}`, `POST /api/v1/terminals/{id}/heartbeat` | ✅ implemented |
| Health | `GET /api/v1/health` | ✅ implemented |
| Payments | `POST /api/v1/payments`, `GET /api/v1/payments/{id}`, `POST /api/v1/payments/{id}/cancel` | ⏳ planned (domain exists, no controller) |
| Transactions | `GET /api/v1/transactions`, `GET /api/v1/transactions/{id}` | ⏳ planned |
| Refunds | `POST /api/v1/refunds`, `GET /api/v1/refunds/{id}` | ⏳ planned |

Until the payment endpoints are implemented, the Android app **simulates
payments and refunds locally** in `PaymentRepository` (Room only). See
[`current-state.md`](current-state.md). The sections below document the target
contract those endpoints will implement.

## Headers

| Header                | Used on              | Meaning                                        |
| --------------------- | -------------------- | ---------------------------------------------- |
| `Authorization`       | all protected routes | `Bearer <jwt>` from login                      |
| `Idempotency-Key`     | `POST /payments`     | client-generated UUID for the logical order    |
| `X-Simulation-Mode`   | `POST /payments`     | `ALWAYS_SUCCESS`, `RANDOM_FAILURE`, `PROCESSOR_DECLINED`, `INSUFFICIENT_FUNDS`, `SLOW_RESPONSE` (dev only) |

## 1. Health & meta

### `GET /api/v1/health`
Service + database status.

```json
{ "status": "ok", "database": "up", "version": "0.1.0", "timestamp": "2026-08-18T10:42:00Z" }
```

## 2. Auth

### `POST /api/v1/auth/login`
Body:
```json
{ "email": "cashier@myshop.com", "password": "secret" }
```
Response `200`:
```json
{
  "accessToken": "<jwt>",
  "expiresIn": 3600,
  "user": { "id": "uuid", "name": "Rahim", "role": "CASHIER", "merchantId": "uuid" }
}
```

### `POST /api/v1/auth/refresh`
Body:
```json
{ "refreshToken": "<refresh>" }
```
Returns a new access token.

## 3. Merchants & terminals

### `POST /api/v1/merchants/register`
Creates a merchant with an OWNER user.

```json
{ "name": "Rahim", "businessName": "Rahim Electronics", "email": "owner@myshop.com", "password": "secret", "phone": "+8801XXXXXXXXX" }
```

### `POST /api/v1/terminals/register`
Pairs a terminal to a merchant using a pairing code.

```json
{ "merchantId": "uuid", "pairingCode": "ABC-1234", "name": "Front Counter" }
```

### `GET /api/v1/terminals/{id}`
Terminal status, `lastHeartbeatAt`, merchant info.

### `POST /api/v1/terminals/{id}/heartbeat`
Registers a heartbeat from the terminal. Response `200` includes server time and
current simulation mode.

## 4. Payments (processing)

### `POST /api/v1/payments`
Create + process a payment. Requires `Idempotency-Key`. Simulated processor
outcome is chosen by the backend (see `X-Simulation-Mode`).

Request:
```json
{
  "amount": 1250.00,
  "currency": "BDT",
  "paymentMethod": "CARD",
  "card": { "number": "4111111111114821", "expiry": "12/29", "holder": "RAHIM AHMED" },
  "terminalId": "uuid",
  "userId": "uuid"
}
```
QR method uses `{ "qrPayload": "..." }`; E_WALLET uses `{ "wallet": { "number": "01XXXXXXXXX" } }`.

Response `201` (created) on first attempt, `200` (existing) when the idempotency
key was already processed:
```json
{
  "transactionId": "uuid",
  "transactionNumber": "TXN-20260818-00021",
  "status": "SUCCESS",
  "amount": 1250.00,
  "currency": "BDT",
  "paymentMethod": "CARD",
  "attempt": {
    "number": 1,
    "maskedReference": "•••• 4821",
    "processorReference": "PR-0000000042",
    "status": "SUCCESS"
  },
  "processedAt": "2026-08-18T10:42:00Z"
}
```

### `GET /api/v1/payments/{transactionId}`
Latest status of a payment attempt flow.

### `POST /api/v1/payments/{transactionId}/cancel`
Cancel a `PENDING` transaction → `CANCELLED`. Returns `409` if not cancellable.

## 5. Transactions (history)

### `GET /api/v1/transactions`
Merchant-scoped history.

| Query param | Meaning                              |
| ----------- | ------------------------------------ |
| `search`    | matches transaction number / masked reference |
| `status`    | `SUCCESS`, `FAILED`, `CANCELLED`, ... |
| `method`    | `CARD`, `QR`, `E_WALLET`             |
| `from` / `to` | date range (ISO 8601)              |
| `page` / `size` | pagination (default 1 / 20)       |

Response:
```json
{
  "items": [ { "transactionId": "uuid", "transactionNumber": "TXN-...", "amount": 1250.00, "currency": "BDT", "paymentMethod": "CARD", "status": "SUCCESS", "createdAt": "..." } ],
  "page": 1, "size": 20, "total": 42
}
```

### `GET /api/v1/transactions/{id}`
Full detail: transaction + attempts + events + refunds (used by detail screen
and receipt).

## 6. Refunds

### `POST /api/v1/refunds`
Request a refund for a `SUCCESS` transaction.

```json
{ "transactionId": "uuid", "amount": 1250.00, "reason": "Customer requested refund" }
```
Response `201`:
```json
{
  "refundNumber": "RFD-20260818-00003",
  "status": "COMPLETED",
  "amount": 1250.00,
  "processedAt": "2026-08-18T11:05:00Z"
}
```

### `GET /api/v1/refunds/{id}`
Refund status lookup.

## 7. Error format

All errors return a consistent shape:
```json
{
  "code": "TRANSACTION_NOT_CANCELLABLE",
  "message": "Only PENDING transactions can be cancelled.",
  "detail": { ... }
}
```
Common codes: `UNAUTHORIZED`, `INVALID_PAYMENT_METHOD`, `IDEMPOTENCY_MISMATCH`,
`AMOUNT_MISMATCH`, `TRANSACTION_NOT_CANCELLABLE`, `NOT_REFUNDABLE`,
`DUPLICATE_EMAIL`, `INVALID_PAIRING_CODE`, `PAYMENT_PROCESSOR_DECLINED`,
`INSUFFICIENT_FUNDS`.

## 8. Status codes

| Code | Meaning                                    |
| ---- | ------------------------------------------ |
| 200  | OK (idempotent replay, lookups)            |
| 201  | Created (first payment, refund)            |
| 400  | Validation failure                         |
| 401  | Missing/invalid JWT                        |
| 404  | Resource not found                         |
| 409  | State conflict (cancel, idempotency key conflict on different payload) |
| 422  | Processor declined / insufficient funds (request valid, business declined) |
| 503  | Backend or database unavailable            |
