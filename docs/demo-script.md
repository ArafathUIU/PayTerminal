PayTerminal v1.1 Demo Script
============================

This script describes the exact screen-by-screen sequence for a 60-120 second
recording of the PayTerminal v1.1 Android application. All demonstrations use
the existing application functionality — no new features or server-driven
integration is required.

--------------------------------------------------------------------
00:00 — Launch
--------------------------------------------------------------------
[ADB]: am start -n com.arafath.payterminalversion2/.MainActivity
[Wait for MainActivity to load]
- Observe the app launching on the emulator
- No splash screen beyond the initial activity load

--------------------------------------------------------------------
00:03 — OWNER Login
--------------------------------------------------------------------
[ADB]: tap on login field / enter credentials
[Type]: farhana.e2e@example.com
[Type]: owner123 (or whatever the OWNER password is)
[Press: Login button]
[Wait for dashboard to load]
- Observe the OWNER role badge visible
- Observe the Terminal tab is accessible

--------------------------------------------------------------------
00:08 — Owner Dashboard
--------------------------------------------------------------------
[Wait for dashboard to stabilize]
- Show the home/dashboard screen
- Point out: merchant name, terminal status, today's totals
- Highlight the OWNER badge in the UI
- Note the available navigation items

--------------------------------------------------------------------
00:12 — Merchant Profile
--------------------------------------------------------------------
[Tap on merchant name/profile area]
[Wait for profile screen / drawer to appear]
- Show the merchant business name
- Point out: "Cached on login via GET /api/v1/merchants/{id}"
- Observe the name persists across screens (Home, Receipts, Terminal Mgmt)

--------------------------------------------------------------------
00:17 — Payment Flow — Payment Screen
--------------------------------------------------------------------
[Tap on Payment CTA]
[Wait for payment amount entry screen]
[Enter a demo amount, e.g. 1250]
[Select Card as payment method]
[Press Pay / Continue]
[Wait for processing state to begin]
- Show the payment amount and method selection
- Observe the processing spinner/indicators appear
- Note: ~1.4s local simulation round-trip

--------------------------------------------------------------------
00:23 — Payment Processing
--------------------------------------------------------------------
[Wait for processing to complete successfully]
- Show the processing state with spinner
- Note the ~1.4s round-trip timing
- Wait for the success state to render

--------------------------------------------------------------------
00:27 — Payment Success
--------------------------------------------------------------------
[Observe success state]
- Show the success screen with receipt option
- Point out: transaction persisted locally (Room)
- Observe the merchant name displayed on the success screen

--------------------------------------------------------------------
00:32 — Transaction History
--------------------------------------------------------------------
[Tap on History / Transactions navigation]
[Wait for history list to load]
- Show the list of transactions
- Locate the just-created transaction
- Point out: filters, columns (Date, Amount, Method, Status, Reference)

--------------------------------------------------------------------
00:38 — Transaction Details
--------------------------------------------------------------------
[Tap on the just-created transaction]
[Wait for details screen]
- Show the transaction details
- Point out: Amount, Method, Status, Reference, Timestamp
- Observe the Receipt button is available
- Observe: Refund button visible (OWNER role)

--------------------------------------------------------------------
00:43 — Receipt
--------------------------------------------------------------------
[Tap on Receipt button]
[Wait for receipt screen]
- Show the digital receipt
- Point out: Merchant name, terminal code, amount, date/time, reference, payment method
- Observe: Share option, View raw data option

--------------------------------------------------------------------
00:48 — CASHIER Login
--------------------------------------------------------------------
[Tap on Logout / Switch User]
[Login as CASHIER]
[Type]: karim.cashier@example.com
[Type]: cashier123 (or whatever the CASHIER password is)
[Press: Login]
[Wait for dashboard to load]
- Show the CASHIER role badge
- Point out: Terminal tab is already hidden/absent
- Point out: Refund button is hidden on transaction details

--------------------------------------------------------------------
00:54 — CASHIER Restriction Demonstration
--------------------------------------------------------------------
[Navigate to a transaction details screen]
[Observe the UI]
- Demonstrate: Refund button is HIDDEN for CASHIER
- Demonstrate: Terminal tab is NOT present in the navigation
- Show the error message if Refund is attempted: "Only the business owner can refund payments"
- Note: CASHIER can still view history and make payments

--------------------------------------------------------------------
01:00 — Switch Back to OWNER
--------------------------------------------------------------------
[Tap on Logout / Switch User]
[Login as OWNER]
[Type]: farhana.e2e@example.com
[Type]: owner123 (or whatever the OWNER password is)
[Press: Login]
[Wait for dashboard to load]
- Show the OWNER role badge is restored
- Terminal tab is visible again
- Refund button is visible on transaction details

--------------------------------------------------------------------
01:05 — Owner Refund
--------------------------------------------------------------------
[Navigate to a successful transaction's details]
[Tap on Refund button]
[Wait for refund entry screen]
[Enter a refund amount, e.g. 500 (≤ original)]
[Press Confirm / Submit]
[Wait for processing]
- Show the refund processing state
- Wait for the refund to complete
- Observe the transaction status changes to REFUNDED

--------------------------------------------------------------------
01:12 — Refund Completed
--------------------------------------------------------------------
[Observe the refunded state]
- Show the transaction now marked as REFUNDED
- Point out: refundedAt timestamp set
- Observe the UI reflects the completed refund
- Note: All state changes are local simulation

--------------------------------------------------------------------
01:18 — Terminal Management
--------------------------------------------------------------------
[Navigate to Terminal Management screen (OWNER only)]
[Wait for screen to load]
- Show the terminal list / management screen
- Point out: Online/Offline status
- Show the last heartbeat / last sync information
- Demonstrate the heartbeat concept: `POST /api/v1/terminals/{id}/heartbeat`

--------------------------------------------------------------------
01:25 — Online/Offline + Last Sync
--------------------------------------------------------------------
[Observe the terminal health status]
- Point out: Fresh heartbeat (< 2 min) = Online
- Point out: Stale heartbeat (> 2 min) = Offline
- Show the "Last sync" relative time
- Note: `Time.relative()` used for display

--------------------------------------------------------------------
01:30 — Card Expiry Format
--------------------------------------------------------------------
[Navigate to payment method selection]
[Demonstrate card expiry input]
- Type digits into the expiry field
- Observe the / auto-insert after first 2 digits
- Type full MM/YY (e.g. 12/25)
- Note: Max 4 digits accepted
- Observe: Backspace correctly handles formatted field

--------------------------------------------------------------------
01:35 — Finish
--------------------------------------------------------------------
[End of recording]
- The demo is complete
- All functionality demonstrated using existing application flows
- No new features implemented
- No server-driven payments or gateways integrated