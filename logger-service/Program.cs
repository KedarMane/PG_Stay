// Tiny standalone logging service for the PG Management System.
//
// It does exactly one thing: whenever the Java backend performs an action (an API
// request), it POSTs a short message here, and this app appends a line to a plain
// text file on disk. That's it - no database, no complex setup.
//
// Run it with:  dotnet run
// It listens on http://localhost:5099 and writes to logger-service/logs/actions.log
//
// The main app (Spring Boot backend) is written to tolerate this service being down -
// if it's not running, requests to the main app still work fine, they just won't be
// logged until this is started again.

using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

// logs/actions.log, relative to wherever you run "dotnet run" from (normally the
// logger-service/ folder itself, per the README) - created automatically if missing.
var logDirectory = Path.Combine(Directory.GetCurrentDirectory(), "logs");
Directory.CreateDirectory(logDirectory);
var logFilePath = Path.Combine(logDirectory, "actions.log");

// File.AppendAllText isn't safe to call from multiple requests at the same time,
// so every write goes through this lock.
var fileWriteLock = new object();

app.MapPost("/api/log", async (HttpContext http) =>
{
    using var reader = new StreamReader(http.Request.Body);
    var body = await reader.ReadToEndAsync();

    string message;
    try
    {
        var entry = JsonSerializer.Deserialize<LogEntry>(
            body, new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
        message = entry?.Message ?? body;
    }
    catch (JsonException)
    {
        // If it wasn't valid JSON for some reason, just log the raw text instead of failing.
        message = body;
    }

    var line = $"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {message}";

    lock (fileWriteLock)
    {
        File.AppendAllText(logFilePath, line + Environment.NewLine);
    }

    return Results.Ok();
});

// Quick way to check the service is up: http://localhost:5099/api/log/health
app.MapGet("/api/log/health", () => Results.Ok("Logger is running. Writing to: " + logFilePath));

app.Run("http://localhost:5099");

record LogEntry(string? Message);
