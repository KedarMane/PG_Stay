package com.pgms.payment.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

// This service has no concept of "which human is logged in" - it never sees a JWT and
// doesn't need Spring Security. Every one of its endpoints is only ever meant to be called
// by the main backend, after the backend has already done real user authentication and
// ownership checks. So instead of a full auth framework, this is a much simpler question:
// "did this request come from our own backend, or from some random caller on the network?"
// answered with one shared-secret header, X-Internal-Token.
//
// Two endpoints are deliberately excluded: /api/payments/webhook (Razorpay's servers call
// this directly and have no way to know our internal token - it's protected instead by its
// own HMAC signature check, see PaymentService#verifyWebhookSignature) and
// /api/payments/health (harmless, useful for a quick "is it up" check without secrets).
//
// A Spring Boot app auto-registers any Filter bean for "/*" by default, so no extra
// FilterRegistrationBean wiring is needed here.
@Component
@Order(1)
public class InternalAuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/payments/webhook",
            "/api/payments/health"
    );

    @Value("${internal.api.token}")
    private String expectedToken;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (PUBLIC_PATHS.contains(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String providedToken = httpRequest.getHeader("X-Internal-Token");
        if (expectedToken == null || expectedToken.isBlank() || !expectedToken.equals(providedToken)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"success\":false,\"message\":\"Missing or invalid internal service token\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
