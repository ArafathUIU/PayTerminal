# PayTerminal — Technology Decisions (ADR)

**Rule:** every technology, framework, library, architectural pattern,
infrastructure component, or major dependency in PayTerminal must have a clear
purpose, and must be something the developer can explain confidently in an
interview. Before introducing anything new, we record:

1. Why it is needed
2. What problem it solves
3. What simpler alternative exists
4. Why the chosen option is preferable *for this project*

We deliberately avoid "enterprise" or popular-but-unnecessary additions. A
smaller, coherent stack is preferred.

Each entry below is dated and immutable-ish; new decisions append, old ones are
superseded rather than rewritten.

---

## ADR-001 — Layered backend (Domain / Application / Infrastructure / Api)
- **Date:** 2026-08-18
- **Why:** The backend models a payment state machine that must be testable and
  isolated from HTTP and persistence concerns.
- **Problem solved:** Keeps business rules out of controllers; allows unit
  testing the state machine without a database; matches the layered structure
  interviewers expect for a payment system.
- **Simpler alternative:** Single-project API with everything in one place.
- **Why chosen:** The state machine + idempotency rules are the core value of
  this project; a single project would bury them. Four small projects is not
  over-engineering for the demonstrable separation it buys.

## ADR-002 — .NET 8 / ASP.NET Core
- **Date:** 2026-08-18
- **Why:** Backend runtime; already installed, LTS, the stack requested.
- **Alternative:** .NET 10 (needs SDK install), Node/FastAPI (different language
  family).
- **Why chosen:** LTS, in place, matches the spec (ASP.NET Core + EF Core).

## ADR-003 — PostgreSQL 16 via Docker Compose
- **Date:** 2026-08-18
- **Why:** Production-grade relational store for the payment data model, with
  real unique constraints for idempotency and proper decimal money types.
- **Alternative:** SQLite, in-memory store, or a local Postgres install.
- **Why chosen:** SQLite can't fully demonstrate unique-index idempotency and
  concurrent transactions the way Postgres can; Docker gives a reproducible
  environment with zero local install. The DB is a real component of the
  project, not a stub.

## ADR-004 — Entity Framework Core + Npgsql
- **Date:** 2026-08-18
- **Why:** Object–relational mapping and migrations between the domain model and
  Postgres.
- **Alternative:** Dapper (manual SQL), raw ADO.NET, or raw SQL migrations.
- **Why chosen:** EF Core gives compile-checked queries, a schema that stays in
  sync with the C# model, and reversible migrations — the fastest path to a
  correct DB contract while remaining an industry-standard ORM to discuss in an
  interview. Dapper would add manual SQL maintenance with no benefit at this
  project's size.

## ADR-005 — ASP.NET Core MVC controllers (not minimal APIs)
- **Date:** 2026-08-18
- **Why:** Endpoint groups map 1:1 to controllers (Auth, Merchants, Terminals,
  Payments, Transactions, Refunds), matching the API contract.
- **Alternative:** Minimal API endpoints.
- **Why chosen:** Controllers provide filters, model binding, and structured
  groups that keep a multi-resource API readable as it grows through Phase 5.
  Minimal APIs shine for tiny APIs; this one is heading toward ~20 endpoints
  across 6 resources. Controllers are also the form most interviewers know.

## ADR-006 — JWT bearer authentication (JwtBearer)
- **Date:** 2026-08-18
- **Why:** The Android terminal needs stateless, scoped auth (user + merchant +
  role) that middleware can validate without a session store.
- **Alternative:** Session cookies (poor fit for a device client), API keys
  (no user/merchant scope).
- **Why chosen:** JWTs carry claims the POS needs (userId, merchantId, role)
  and are the standard for mobile-to-API auth. Refresh tokens (stored in the
  DB) cover expiry without forcing frequent re-login.

## ADR-007 — ASP.NET `PasswordHasher<T>` (no BCrypt package)
- **Date:** 2026-08-18
- **Why:** Secure password hashing.
- **Alternative:** BCrypt.Net, PBKDF2 via raw crypto.
- **Why chosen:** Built into ASP.NET Core (no third-party package), uses PBKDF2
  with per-user salt, and is audited by Microsoft. One less dependency, same
  security posture.

## ADR-008 — DataAnnotations validation (no FluentValidation)
- **Date:** 2026-08-18
- **Why:** Request validation on DTOs.
- **Alternative:** FluentValidation, manual validation in services.
- **Why chosen:** Our DTOs are simple (required fields, ranges, a currency
  format). DataAnnotations is built in, declarative, and automatically enforced
  by MVC. FluentValidation's expressive power would be unused here and is the
  kind of "popular enterprise" dependency we avoid.

## ADR-009 — Manual DTO mapping (no AutoMapper)
- **Date:** 2026-08-18
- **Why:** Convert entities ↔ API DTOs.
- **Alternative:** AutoMapper, manual mapping.
- **Why chosen:** Mapping is 1:1 and small in count. Manual mapping is
  transparent, dependency-free, and lets us shape response contracts exactly.
  AutoMapper would add configuration magic for no measurable benefit.

## ADR-010 — xUnit for unit tests
- **Date:** 2026-08-18
- **Why:** Verify state-machine transitions and auth logic.
- **Alternative:** MSTest, NUnit.
- **Why chosen:** xUnit is the de-facto standard for .NET and pairs cleanly with
  the rest of the stack. Test project is small; the framework choice is
  low-stakes but xUnit is the safest common ground.

## ADR-011 — Swashbuckle (Swagger UI / OpenAPI)
- **Date:** 2026-08-18
- **Why:** Interactive API docs for the simulator; lets us demo and hand-test
  every endpoint.
- **Alternative:** Postman collections, plain docs.
- **Why chosen:** Zero-config OpenAPI output for an API whose contract is a
  deliverable (docs/api-contract.md). Generated spec can later drive client
  verification.
