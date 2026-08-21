# PGMS Payment Service

A standalone Spring Boot microservice with one bounded context: **collecting rent through
Razorpay**. It knows about bookings only as an ID number and an amount - nothing about
Users, Properties, Rooms, Beds, or discount plans. That domain knowledge stays in the main
backend (`../backend`), which computes what a booking owes and calls this service to
actually collect it.

## Why this is a separate service, not just a package

- **Own database.** `payment_service_db` is a different MySQL database from the main
  backend's `pgms_db`. No cross-database joins, no shared tables - the payment ledger is
  this service's alone, reached only through its HTTP API.
- **Own deployable.** It's a separate JAR with its own `main()`, own port (`8081`), and can
  be started, stopped, redeployed, or scaled independently of the main backend.
- **Own (much simpler) trust model.** The main backend authenticates real users with JWT
  and Spring Security. This service has no users at all - it only ever gets called by the
  main backend, so it uses one shared-secret header (`X-Internal-Token`) instead. See
  `InternalAuthFilter`.

## Prerequisites

- Java 17+ and Maven
- MySQL running locally (same instance the main backend already uses is fine - it just
  needs a *different* database name, `payment_service_db`, which is created automatically)
- A Razorpay test account/keys (same ones the monolith used to use)

## Setup

1. Edit `src/main/resources/application.properties`:
   - Set `razorpay.key-id`, `razorpay.key-secret`, `razorpay.webhook-secret` (from
     https://dashboard.razorpay.com/app/keys).
   - Set `internal.api.token` to any long random string.
2. In the **main backend's** `application.properties`, set
   `payment.service.internal-token` to that exact same string, and
   `payment.service.base-url=http://localhost:8081`.

## Run it

```bash
cd payment-service
mvn spring-boot:run
```

Starts on `http://localhost:8081`. Run this **alongside** the main backend (`:8080`) and,
if you want request logging, the logger service (`:5099`) - unlike the logger service,
though, the main backend's payment flow is a hard dependency: if this service is down,
"Pay rent" in the frontend will fail (the backend surfaces a clear "payment service is
currently unavailable" message rather than a raw connection error).

## Endpoints

All require the `X-Internal-Token` header (set to the same value as `internal.api.token`)
**except** `/api/payments/webhook` and `/api/payments/health`.

| Method | Path                              | Called by                              |
|--------|-----------------------------------|-----------------------------------------|
| POST   | `/api/payments/orders`            | Backend, when a guest clicks "Pay rent" |
| POST   | `/api/payments/verify`            | Backend, after Razorpay Checkout succeeds |
| GET    | `/api/payments/bookings/{id}`     | Backend, for a booking's payment history |
| GET    | `/api/payments`                   | Backend, for the admin "all payments" screen |
| POST   | `/api/payments/webhook`           | Razorpay's servers directly (public, HMAC-checked) |
| GET    | `/api/payments/health`            | Anyone, quick liveness check |

The frontend never calls this service directly - it still only ever talks to the main
backend on `:8080`, which proxies the payment endpoints through. See
`backend/.../client/PaymentServiceClient.java` for that side of the call.
