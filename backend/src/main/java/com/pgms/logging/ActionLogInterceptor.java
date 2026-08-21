package com.pgms.logging;

import com.pgms.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fires off one message per request to a small standalone .NET service (see
 * /logger-service in the project root), which is the thing that actually appends a
 * line to a plain text log file on disk.
 * <p>
 * This is intentionally fire-and-forget with a short timeout: if that service isn't
 * running, the main app must keep working normally - it just won't get logged until
 * the logger service is started again.
 */
@Component
public class ActionLogInterceptor implements HandlerInterceptor {

    private static final String LOGGER_SERVICE_URL = "http://localhost:5099/api/log";
    private static final String START_TIME_ATTR = "com.pgms.actionLog.startNanos";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(500))
            .build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startAttr = request.getAttribute(START_TIME_ATTR);
        long durationMs = startAttr instanceof Long start ? (System.nanoTime() - start) / 1_000_000 : -1;

        String who = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            who = principal.getUser().getEmail() + " (" + principal.getUser().getRole() + ")";
        }

        String outcome = (ex == null && response.getStatus() < 400) ? "OK" : "ERROR";

        String message = String.format("%s | %s %s | status=%d | user=%s | %dms",
                outcome, request.getMethod(), request.getRequestURI(), response.getStatus(), who, durationMs);

        sendToLoggerService(message);
    }

    private void sendToLoggerService(String message) {
        try {
            String json = "{\"message\":\"" + escapeJson(message) + "\"}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(LOGGER_SERVICE_URL))
                    .timeout(Duration.ofSeconds(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            // Fire-and-forget: never block the actual response waiting on the logger service.
            httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(e -> null);
        } catch (Exception ignored) {
            // Logging must never break the actual request, so any failure here is swallowed.
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
