# PG_Stay

Staylist digitizes the full PG accommodation workflow: Admins define serviceable locations and approve property listings; Owners list properties, manage rooms and beds, and approve guest bookings; Guests search properties, request beds, and pay rent monthly through an integrated payment gateway. A separate lightweight .NET service logs every API action for auditability without touching core business logic.

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
