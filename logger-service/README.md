# PGMS Action Logger (.NET)

A tiny standalone service with one job: receive a short message every time an action
happens in the main app, and append it as a line in a plain text file on disk.

## Prerequisites
- [.NET 10 SDK](https://dotnet.microsoft.com/download/dotnet/10.0) (or edit the
  `TargetFramework` in `pgms-logger.csproj` to match whatever .NET version you already have —
  run `dotnet --list-runtimes` to check)

## Run it

```bash
cd logger-service
dotnet run
```

It starts on `http://localhost:5099`. Logs are written to `logger-service/logs/actions.log`,
one line per action, e.g.:

```
[2026-07-29 10:15:22] OK | POST /api/guest/bookings | status=200 | user=alice@example.com (GUEST) | 42ms
[2026-07-29 10:16:05] ERROR | POST /api/payments/verify | status=400 | user=alice@example.com (GUEST) | 310ms
```

Just open `logs/actions.log` in any text editor to read it.

## How it's used

The Java backend (`backend/`) calls `POST http://localhost:5099/api/log` with a small
JSON body (`{"message": "..."}`) after every request, using a short timeout and
fire-and-forget style — see `com.pgms.logging.ActionLogInterceptor` in the backend.

This means:
- **Run this service alongside the backend** if you want logs recorded.
- If it isn't running, the main app is unaffected — requests still work normally,
  they just won't be logged until this service is started again.

## Health check

`GET http://localhost:5099/api/log/health` returns a 200 with a short message confirming
it's up and shows exactly where the log file is being written.
