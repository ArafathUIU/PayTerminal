# PayTerminal

Android POS payment terminal simulator.

## 🎥 Demo

> 📹 **Full application walkthrough:**  
> I will record a manual video demonstration after this closeout.  
> Place the final recording at: `docs/demo/payterminal-v1.1-demo.mp4`  
> The demo script is at: `docs/demo-script.md`

Suggested recording flow:

1. Login (Owner/Cashier)
2. Dashboard — merchant profile, terminal status
3. Payment flow — Card / QR / E‑Wallet amount entry
4. Processing state → Success / Failed
5. Transaction history list
6. Receipt screen
7. CASHIER‑only restrictions (Terminal tab hidden, Refund button hidden)
8. Switch to OWNER role
9. OWNER‑only refund
10. Terminal Management — Online/Offline + Last sync

## 📱 UI Showcase

All screenshots captured from the running emulator on a 1080×2400 device.

### Authentication

#### Login

![Login](docs/screenshots/01-login-owner.png)

**What it shows:** The sign‑in screen uses real JWT authentication against the ASP.NET Core backend.

- **Fields:** Email, Password
- **Available roles:** `OWNER` (Farhana / farhana.e2e@example.com) and `CASHIER` (Karim / karim.cashier@example.com)
- **Post‑login:** role‑based UI gates are enforced (see Role‑Based Access Control below)

#### CASHIER Login

![CASHIER Dashboard](docs/screenshots/10-cashier-dashboard.png)

**What it shows:** CASHIER‑restricted dashboard after login.

- Same login flow, different role enforcement

### Dashboard & Merchant

#### OWNER Dashboard

![Owner Dashboard](docs/screenshots/02-owner-dashboard.png)

**What it shows:** The home dashboard displays the merchant's business name, terminal status, and today's totals.

- **Elements:** Merchant name, terminal code/code status, OWNER/CASHIER badge, transaction summary cards
- **Role behavior:**
  - **OWNER:** sees **Terminal** tab, **Refund** button on transaction details, full Terminal Management
  - **CASHIER:** Terminal tab **hidden**, Refund button **hidden**, restricted to Payments + History only

#### Merchant Profile

![Merchant Profile](docs/screenshots/03-merchant-profile.png)

**What it shows:** The registered business name and contact information. The business name is cached on login (fetched via `GET /api/v1/merchants/{id}`) so it appears consistently on Home, Receipts, and Terminal Management.

- **Merchant retrieval:** Fetched from backend on login
- **Merchant caching:** Stored in Room local database for persistent access

### Payments

#### Payment

![Payment](docs/screenshots/04-payment.png)

**What it shows:** The payment screen allows the merchant to enter an amount and select a payment method.

- **Amount entry:** numeric keypad, auto‑formatted expiry MM/YY for Card payments
- **Methods:** Card, QR, E‑Wallet
- **Processing:** local simulation (~1.4 s round‑trip); decline rule `amountPaise % 100 == 99`

#### Payment Processing

![Payment Processing](docs/screenshots/05-payment-processing.png)

**What it shows:** During processing, a simulated processor round‑trip occurs. The UI shows spinners/indicators while the local `PaymentRepository.process()` coroutine runs.

- **No real bank/MFS/payment gateway integration** — all processing is local simulation

#### Payment Success

![Payment Success](docs/screenshots/06-payment-success.png)

**What it shows:** Transaction persisted in Room, receipt generated, merchant name displayed.

- **Success:** transaction status `SUCCESS`, receipt rendered
- **Failure:** transaction status `FAILED`; retry option available

### Transaction History

#### Transaction History

![Transaction History](docs/screenshots/07-transaction-history.png)

**What it shows:** List of all transactions for the signed‑in merchant.

- **Filters:** search by transaction ID/reference, payment method, status (ALL/SUCCESS/FAILED/REFUNDED)
- **Columns:** Date, Amount, Method, Status, Reference
- **Role behavior:** Both **OWNER** and **CASHIER** can view history; role‑based restrictions apply to Refund and Terminal Management, not history

#### Transaction Details

![Transaction Details](docs/screenshots/08-transaction-details.png)

**What it shows:** Details screen for a single transaction.

- **Shows:** Amount, Method, Status, Reference, Merchant name, Receipt button
- **Refund button:** hidden for CASHIER; visible for OWNER (enforced by `RefundViewModel` and `TerminalManagementFragment` as safety net)

#### Receipt

![Receipt](docs/screenshots/09-receipt.png)

**What it shows:** Digital receipt generated after a successful payment.

- **Contains:** Merchant business name, terminal code, amount, date/time, reference, payment method, itemization
- **Actions:** Share receipt, View raw data
- **Merchant name:** fetched from the backend on login; cached in Room

### Refunds

#### Refund

![Refund](docs/screenshots/12-refund.png)

**What it shows:** Refund screen accessible from transaction details.

- **OWNER:** can enter refund amount (≤ original amount), submit refund
- **CASHIER:** Refund button hidden; `RefundViewModel.confirm()` returns error `"Only the business owner can refund payments"`
- **Process:** local simulation — transaction status moves `SUCCESS → REFUNDED`, `refundedAt` timestamp set
- **Result:** receipt/refund record updated; UI reflects new state

#### Refund Completed

![Refund Completed](docs/screenshots/13-refund-completed.png)

**What it shows:** Transaction moved to REFUNDED state; UI reflects the completed refund.

- Refund state machine: `Requested → Processing → Completed / Rejected`
- **No real financial refund** — all refunds are simulated locally

### Role‑Based Access Control

| Feature                    | OWNER | CASHIER |
| ------------------------ | :---: | :---: |
| Login                    | ✅    | ✅    |
| Payments (Card/QR/Wallet)| ✅    | ✅    |
| Transaction History      | ✅    | ✅    |
| **Refund**               | ✅    | ❌    |
| **Terminal Management**  | ✅    | ❌    |
| Card expiry formatting   | ✅    | ✅    |

- **OWNER:** Payment, Refund, Terminal Management, full access
- **CASHIER:** Payment + History only; Refund and Terminal Management buttons hidden and gated in ViewModels

### Terminal Management

#### Terminal Management

![Terminal Management](docs/screenshots/14-terminal-management.png)

**What it shows:** Terminal Management screen visible ONLY to OWNER role.

- **Terminal list:** shows paired terminals, status, last heartbeat
- **Online/Offline:** determined by heartbeat freshness (< 2 min = Online, otherwise Offline)
- **Last sync:** relative time since last heartbeat (`Time.relative()`)
- **Heartbeat:** `POST /api/v1/terminals/{id}/heartbeat` keeps the live status current

#### Terminal Health

![Terminal Health](docs/screenshots/15-terminal-health.png)

**What it shows:** Online/Offline status and last heartbeat / last sync information.

- **Online/Offline:** `< 2 min` fresh heartbeat = Online, otherwise Offline
- **Last sync:** relative time since last heartbeat

### Card Expiry Auto‑Format

![Expiry Format](docs/screenshots/16-expiry-format.png)

**What it shows:** The Card Payment screen includes an expiry field that auto‑formats as `MM/YY`.

- User types digits; a `/` is inserted after the first two digits
- Maximum of 4 digits accepted
- Backspace correctly handles the formatted field
- The formatted expiry is passed to the payment simulation

## 💳 Payment Flow

```text
User
  ↓
Payment Screen (amount + method selection)
  ↓
Processing (~1.4s local simulation)
  ↓
Success / Failed
  ↓
Room Persistence (PaymentTransactionEntity)
  ↓
Transaction History (filtered/list view)
  ↓
Receipt (generated on success)
```

**Important:** PayTerminal v1.1 performs payment processing as a **LOCAL SIMULATION**.

- No real bank deduction
- No real MFS deduction
- No real payment gateway
- No real money movement
- The application simulates the payment lifecycle and persists the transaction locally via Room

## ↩️ Refund Flow

```text
OWNER
  ↓
Select Transaction from history
  ↓
Refund Request (enter amount ≤ original)
  ↓
Processing (local simulation)
  ↓
Completed / Rejected
  ↓
Local Transaction Update (status → REFUNDED)
  ↓
Receipt/Refund record updated; UI reflects new state
```

**Important:** PayTerminal v1.1 refunds are **simulated locally**.

- No real financial reversal
- No real money movement
- The refund state machine is: `Requested → Processing → Completed / Rejected`
- The transaction status moves `SUCCESS → REFUNDED`, `refundedAt` timestamp set
- All state changes are persisted locally in Room

## 👥 Role Demonstration

| Feature                    | OWNER | CASHIER |
| ------------------------ | :---: | :---: |
| Login                    | ✅    | ✅    |
| Payment                  | ✅    | ✅    |
| Transaction History      | ✅    | ✅    |
| Refund                   | ✅    | ❌    |
| Terminal Management      | ✅    | ❌    |
| Card expiry formatting   | ✅    | ✅    |

**Verification:** These restrictions were tested and confirmed on the emulator:

- **CASHIER:** Terminal tab hidden, Refund button hidden on transaction details, `RefundViewModel.confirm()` returns `"Only the business owner can refund payments"`
- **OWNER:** Full access to Terminal tab, Refund button visible and functional, Terminal Management operational

## 🏗️ Architecture

```yaml
Android App
    ↓
Repositories (Room, Retrofit, Hilt)
    ↓
Domain State Machines (Transaction + Refund)
    ↓
Local Persistence (Room — PostgreSQL mirror)
```

**Server‑side (ASP.NET Core + PostgreSQL):**

- JWT authentication & token refresh
- Merchant registration + `GET /api/v1/merchants/{id}`
- Terminal registration + pairing + `POST /api/v1/terminals/{id}/heartbeat`
- Domain state machines (Transaction + Refund) — pure business logic, no UI
- Role constants: `Owner` / `Cashier`
- No payment‑processing gateway integration (v1.1 local simulation)

**Local v1.1:**

- Payment simulation — local only, no external gateway
- Refund simulation — local only, no external reversal
- Room transaction persistence — source of truth for Android app
- Merchant caching on login — fetched from backend, stored locally
- Terminal heartbeat monitoring — `POST /api/v1/terminals/{id}/heartbeat`
- Online/Offline status — determined by heartbeat freshness
- Card expiry MM/YY auto‑format — local formatting rule
- Role‑based UI gates — enforced both in Fragments and ViewModels

**Future v2.0:**

- Server‑driven payments API with real payment gateway integration
- Server‑driven refunds with real transaction reversal
- Transaction synchronization & reconciliation database
- Real settlement and fraud/risk management
- PCI‑compliant card data handling considerations

## 📦 Release

- **Version:** v1.1 (Phase 8 Polish)
- **APK:** `app-release-signed.apk` (signed with keystore, zipaligned)
- **Release notes:** Merchant profile on login, health monitoring, expiry auto‑format MM/YY, unit tests, role restrictions, API additions

## 📜 License

MIT License

## 📧 Contact

Portfolio project — contact via GitHub repository.