<<<<<<< HEAD
# Staylist — PG Management System

Full-stack PG (Paying Guest) accommodation platform with three roles — **Admin**, **Owner**,
**Guest** — built as:

- **Backend:** Spring Boot 3 (Java 17) + Spring Security (JWT) + MySQL + JPA/Hibernate
- **Payment microservice:** a separate Spring Boot app (`payment-service/`) that owns rent
  collection via Razorpay end-to-end — its own JAR, its own port (`8081`), its own MySQL
  database (`payment_service_db`). The main backend never talks to Razorpay directly anymore;
  it delegates to this service over an internal HTTP call. See section 4 below.
- **Frontend:** React 18 + **Vite** + React Router + Axios, responsive custom CSS (no UI framework)

---

## What changed in this version

Two real bugs from the first pass are fixed here:

1. **500 errors from the API** — the original controllers returned JPA entities directly as JSON.
   Entities like `Property → Room → Bed → Room → Property` have bidirectional relationships that
   recurse infinitely when Jackson serializes them, and lazy-loaded fields could throw outside a
   transaction. **Every endpoint now returns a plain DTO** (`dto/response/*.java`) built from the
   entity, never the entity itself. This also stops the API from ever leaking a user's password
   hash in a response.
2. **Owners couldn't see locations to add a property** — the "add property" dropdown called an
   admin-only endpoint. There's now a dedicated public `/api/locations` read-only endpoint any
   logged-in role can call.

The frontend is also rebuilt on **Vite** instead of Create React App — faster dev server, no
deprecated-package warnings, and `.jsx` files instead of `.js` for anything containing JSX
(Vite's convention).

---

## 1. How the flow works

1. **Admin** logs in (a default admin account is auto-seeded on first run) and adds **Locations**.
2. **Owner** registers, completes KYC profile (govt ID etc.), and adds a **Property** under one
   of the admin's locations. The property starts as `PENDING`.
3. **Admin** reviews the property + owner KYC and **approves or rejects** it. Rejected properties
   can be edited and resubmitted by the owner.
4. Once `APPROVED`, the owner adds **Rooms** (Private or Shared). Shared rooms contain individual
   **Beds**, each with its own rent and status — booking is **bed-level**, not room-level.
5. **Guest** registers, completes their own KYC profile, searches properties by name/location, and
   sends a **booking request** for a specific bed with a check-in date (stays are open-ended — no
   fixed check-out).
6. First guest to request a bed reserves it (**FCFS**) — the bed is marked `PENDING` and any other
   request for the same bed is rejected immediately until the owner decides.
7. **Owner** reviews the guest's govt ID and **approves or rejects** the booking. Rejecting frees
   the bed back up.
8. Once approved, the guest pays each billing month's rent as a one-time payment via Razorpay
   Checkout (Orders API) — "Pay rent" for the current month, verified server-side, repeated
   each month rather than one long-lived auto-debit mandate. As of this version, the actual
   Razorpay integration and payment ledger live in the standalone **payment-service**
   microservice — the main backend still decides *how much* is owed (bed rent minus any
   discount) and *who's allowed to pay*, then hands that off over an internal API call. See
   section 4.
9. **Admin** has read-only visibility into every property, booking, and payment on the platform.

Facilities (e.g. "added a swimming pool") can be added or removed by the owner **at any time**
after approval — only the core property submission goes through the admin approval workflow.

---

## 2. Project structure

```
pg-management-system/
├── backend/            Spring Boot API (:8080) — the frontend's only entry point
│   └── src/main/java/com/pgms/
│       ├── entity/         JPA entities (internal use only — never returned by controllers)
│       ├── dto/
│       │   ├── request/    what the client sends in
│       │   └── response/   what the API sends back — always a DTO, never an entity
│       ├── repository/     Spring Data repositories
│       ├── service/         business logic (works with entities) — RentPricingService is
│       │                    the only payment-adjacent thing left here (see below)
│       ├── client/          PaymentServiceClient — the only way this app talks to
│       │                    payment-service, over plain HTTP with a shared internal token
│       ├── controller/      REST endpoints (maps entity → DTO before returning)
│       ├── security/        JWT filter, UserDetails, JwtUtil
│       ├── config/          SecurityConfig, DataSeeder, RestClientConfig
│       └── exception/       global exception handling (logs full stack trace server-side)
│
├── payment-service/    Standalone Spring Boot microservice (:8081) — see section 4 below
│   └── src/main/java/com/pgms/payment/
│       ├── entity/          PaymentTransaction (bookingId is a plain Long — no cross-service
│       │                    JPA relationship; this service has its own database)
│       ├── dto/, repository/, service/, controller/, exception/   same layered shape as
│       │                    the main backend, on purpose — easy to compare side by side
│       └── config/          InternalAuthFilter — shared-secret check instead of JWT
│
└── frontend/            React app (Vite) — unchanged by the payment-service split; it only
    └── src/                 ever talks to the backend on :8080
        ├── api/          axios instance + endpoint functions
        ├── context/      AuthContext.jsx (JWT session)
        ├── components/   Navbar, ProtectedRoute, cards, badges...
        ├── pages/        one file per screen, role-organized
        └── styles/       global.css (design tokens, responsive)

logger-service/        Standalone .NET action logger (see section 7 below)
├── Program.cs          One endpoint: POST /api/log -> appends a line to logs/actions.log
└── pgms-logger.csproj
```

---

## 3. Backend setup

### Prerequisites
- Java 17+
- Maven 3.8+ (or use your IDE's built-in Maven, e.g. STS/Eclipse/IntelliJ)
- MySQL 8+ running locally

### Steps

```bash
cd backend
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS pgms_db;"
```

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
server.port=8080

jwt.secret=replace-with-a-long-random-string-at-least-32-chars

# Must match payment-service's internal.api.token exactly - see section 4.
payment.service.internal-token=replace-with-a-long-random-shared-secret
```

Then run it (from a terminal, or your IDE's "Run" button on `PgmsApplication.java`):
```bash
mvn spring-boot:run
```

Confirm you see `Started PgmsApplication` and `>>> Default admin created: admin@pgms.com / Admin@123`
in the log with no red stack trace.

**If you change `server.port`** away from 8080, remember to update `VITE_API_BASE_URL` in the
frontend's `.env` (below) to match.

**Note:** the backend no longer holds any Razorpay keys — those moved to `payment-service`
(section 4). "Pay rent" won't work until that service is also running, though every other
part of the app (bookings, KYC, property approval, etc.) works fine without it.

---

## 4. Payment microservice setup

A separate Spring Boot app that owns everything to do with collecting rent: creating
Razorpay orders, verifying payment signatures, storing the payment ledger, and handling
Razorpay's webhook. See `payment-service/README.md` for the full explanation of *why* this
is split out; this section is just the run-book.

### Prerequisites
- Java 17+, Maven 3.8+ (same as the backend)
- MySQL 8+ running locally — it creates its own database, `payment_service_db`, automatically
- A Razorpay test account/keys (same ones you'd have used with the old monolith build)

### Steps

Edit `payment-service/src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
server.port=8081

razorpay.key-id=rzp_test_yourkeyid
razorpay.key-secret=yoursecretkey
razorpay.webhook-secret=yourwebhooksecret

# Must match payment.service.internal-token in backend/application.properties exactly.
internal.api.token=replace-with-a-long-random-shared-secret
```

Then run it:
```bash
cd payment-service
mvn spring-boot:run
```

Confirm you see `Started PaymentServiceApplication` with no red stack trace, then check
`http://localhost:8081/api/payments/health` in a browser — it should return
`payment-service is up`.

**Run order doesn't matter** — the backend and payment-service can be started in either
order or restarted independently. If payment-service isn't running, every other feature of
the app still works; only "Pay rent" will fail, with a clear "payment service is currently
unavailable" message rather than a crash.

---

## 5. Frontend setup (Vite)

### Prerequisites
- Node.js 18+

### Steps

```bash
cd frontend
npm install
cp .env.example .env
```

Open `.env` and make sure the port matches your backend:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

Then start it:
```bash
npm run dev
```

Vite runs on **http://localhost:5173** by default (not 3000 — that was CRA's port). Open that URL.

For a production build:
```bash
npm run build
npm run preview   # serves the built dist/ folder locally to sanity-check it
```

---

## 6. Debugging a 500 error, if you ever hit one again

Every unhandled exception is now logged **server-side with its full stack trace** before the API
returns a generic message to the browser. If something breaks:

1. Look at the **backend terminal/console** (not the browser) — the real Java exception and stack
   trace will be printed there via `GlobalExceptionHandler`. For anything payment-related,
   also check the **payment-service terminal/console** — it has its own copy of
   `GlobalExceptionHandler` and logs independently, since it's a separate process.
2. Copy the exception name and the first few lines of the `Caused by:` chain — that pinpoints the
   exact class and line.
3. The browser/network tab will only ever show a generic "Something went wrong on our end" message
   by design — internal details are intentionally not leaked to the client.

---

## 7. Action logging (.NET)

Every API request is logged as a plain-text line by a small, separate **.NET** service —
kept deliberately simple: one file, one endpoint, no database.

```bash
cd logger-service
dotnet run
```

Requires the [.NET 10 SDK](https://dotnet.microsoft.com/download/dotnet/10.0) (or whatever
.NET version you already have — see `logger-service/README.md` if you need to retarget it). It
listens on
`http://localhost:5099` and appends one line per action to `logger-service/logs/actions.log`,
e.g.:

```
[2026-07-29 10:15:22] OK | POST /api/guest/bookings | status=200 | user=alice@example.com (GUEST) | 42ms
```

The Java backend calls this service after every request (`com.pgms.logging.ActionLogInterceptor`),
fire-and-forget with a short timeout — if the logger service isn't running, the main app keeps
working normally, it just won't be logged until you start it again. See `logger-service/README.md`
for details.

---

## 8. Key design decisions baked into this build

| Decision | Why |
|---|---|
| DTOs on every response | Prevents infinite-recursion serialization crashes and password leaks |
| Bed-level booking | Each bed in a shared room is bookable independently |
| Open-ended stays | `checkInDate` only, no forced checkout date |
| FCFS concurrency | First request "reserves" a bed (`PENDING`); later requests for the same bed are rejected until the owner decides |
| Rejections are resubmittable | Rejected properties/bookings return to an editable state instead of dead-ending |
| Facilities editable anytime | Only the core property listing needs admin re-approval |
| Direct-to-owner payouts | Modeled via Razorpay Route (owner's own linked account), not custodied by the platform |
| Payment as a microservice | Rent collection is its own bounded context — a "collect X for reference Y" concern that doesn't need to know about beds, rooms, or discount plans. Splitting it out gives it its own deploy cycle, its own database, and a much simpler trust model (see below) than the JWT-authenticated rest of the app |
| Database per service | `payment_service_db` is a separate MySQL database from `pgms_db`. payment-service's `PaymentTransaction.bookingId` is a plain `Long`, not a JPA `@ManyToOne` — no service is ever allowed to query another service's tables directly, only through its API |
| Monolith stays the front door | The frontend still only ever calls `:8080`. The backend proxies payment calls to payment-service internally via `PaymentServiceClient`, so the frontend, its JWT auth, and its CORS config are all completely unaffected by the split |
| Shared-secret internal auth | payment-service has no concept of a logged-in user, so it doesn't need Spring Security or JWT. It trusts one header, `X-Internal-Token`, checked by a lightweight `Filter` (`InternalAuthFilter`) instead — a deliberately simpler mechanism for a "service calls service" trust boundary, distinct from the "human calls service" JWT boundary in front of the backend |

---

## 9. What's not included (by design, as a starting scaffold)

- Real file upload for govt ID docs / property images (currently accepts URLs)
- Rate limiting, refresh tokens, password reset flow
- Automated tests
- A proper admin "change password" flow (the seeded admin password should not stay as-is)
- Full Razorpay Route transfer wiring once you've confirmed settlement timing with Razorpay
- Pagination on search/list endpoints for scale
- Service discovery / config server (Eureka, Spring Cloud Config) — payment-service's URL is
  a hardcoded `localhost:8081` in the backend's properties, fine for two services running on
  one machine, but the next thing you'd reach for with three or more
- A circuit breaker (e.g. Resilience4j) around `PaymentServiceClient` — right now a slow or
  down payment-service just times out per-request (see `RestClientConfig`); a circuit breaker
  would stop hammering it with new requests once it's clearly unhealthy
- Distributed transaction handling — if payment-service crashes right after saving a `PAID`
  row but before returning the response, the backend has no automatic way to reconcile that;
  this is the classic trade-off of splitting a database in two, worth knowing how to explain
  even though this build doesn't solve it (an outbox pattern or saga would be the real fix)
=======
# PG_Stay

PG Stay digitizes the full PG accommodation workflow: Admins define serviceable locations and approve property listings; Owners list properties, manage rooms and beds, and approve guest bookings; Guests search properties, request beds, and pay rent monthly through an integrated payment gateway. A separate lightweight .NET service logs every API action for auditability without touching core business logic.

Features
Role-based access — Admin, Owner, and Guest each get a dedicated dashboard and permission set, enforced via Spring Security + JWT.
Location & property approval workflow — Owners submit properties under admin-defined locations; Admin approves or rejects, with rejected listings returned as editable rather than dead-ended.
Bed-level booking — Shared rooms contain individually bookable beds, each with its own rent and status; bookings are first-come-first-served, with the first request locking a bed as PENDING until the owner decides.
KYC verification — Both Owners and Guests complete a KYC profile (government ID, etc.) reviewed as part of the approval flow.
Rent payments — Guests pay each billing month's rent as a one-time payment via Razorpay Checkout, verified server-side, with payouts modeled to owners via Razorpay Route.
Flexible facility management — Owners can add or remove property facilities anytime without triggering re-approval.
Safe API responses — Every endpoint returns a dedicated DTO instead of a raw JPA entity, preventing infinite-recursion serialization errors and password-hash leaks.
Centralized error handling — Unhandled exceptions are logged server-side with full stack traces; the client only ever receives a generic, safe error message.
Action audit logging — A standalone .NET microservice records every API request (endpoint, status, user, duration) to a plain-text log, called fire-and-forget so a logger outage never affects the core app.
Tech Stack

Backend

Java 21, Spring Boot
Spring Security (JWT-based authentication)
Spring Data JPA / Hibernate
MySQL
Razorpay Java SDK
Maven

Frontend

React Vite
React Router
Axios
Custom responsive CSS (no UI framework)

Tooling

Git / GitHub
How It Works
Admin logs in (a default admin is auto-seeded on first run) and adds Locations.
Owner registers, completes KYC, and adds a Property under an admin-defined location. The property starts as PENDING.
Admin reviews the property and owner KYC, then approves or rejects it.
Once approved, the owner adds Rooms (Private or Shared); shared rooms contain individual Beds.
Guest registers, completes KYC, searches properties, and sends a booking request for a specific bed.
The first request for a bed reserves it (FCFS); later requests for the same bed are rejected until the owner decides.
Owner approves or rejects the booking based on the guest's KYC.
Once approved, the guest pays each month's rent via Razorpay Checkout, verified server-side.
Admin retains read-only visibility into every property, booking, and payment.
>>>>>>> f6352a8cf3b3da8b8ff9036f19d5679705beb5b4
