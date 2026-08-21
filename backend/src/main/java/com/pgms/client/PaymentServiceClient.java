package com.pgms.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgms.dto.request.PaymentVerifyRequest;
import com.pgms.dto.response.PaymentOrderResponse;
import com.pgms.dto.response.PaymentTransactionResponse;
import com.pgms.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The backend's only way of talking to the payment microservice. Every "payment" concept
 * PaymentController and AdminController deal with is really just this class making a plain
 * HTTP call to payment-service and translating the result (or failure) back into the same
 * exceptions the rest of this codebase already knows how to handle.
 * <p>
 * Auth here is deliberately not JWT: payment-service doesn't know what a "user" is, so we
 * send one shared-secret header instead (X-Internal-Token) - see InternalAuthFilter on the
 * payment-service side for the other end of this handshake.
 */
@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${payment.service.base-url}")
    private String baseUrl;

    @Value("${payment.service.internal-token}")
    private String internalToken;

    // Called once the backend has already validated the booking and computed the amount
    // owed (see RentPricingService) - this call just tells payment-service "collect this
    // amount, for this bookingId, for this billing month" and hands back the Razorpay order.
    public PaymentOrderResponse createOrder(Long bookingId, BigDecimal amount, String billingMonth) {
        Map<String, Object> body = Map.of(
                "bookingId", bookingId,
                "amount", amount,
                "billingMonth", billingMonth
        );
        return exchange(HttpMethod.POST, "/api/payments/orders", body, PaymentOrderResponse.class);
    }

    public PaymentTransactionResponse verifyPayment(PaymentVerifyRequest request) {
        return exchange(HttpMethod.POST, "/api/payments/verify", request, PaymentTransactionResponse.class);
    }

    public List<PaymentTransactionResponse> getPaymentsForBooking(Long bookingId) {
        return exchangeList("/api/payments/bookings/" + bookingId);
    }

    public List<PaymentTransactionResponse> getAllPayments() {
        return exchangeList("/api/payments");
    }

    private <T> T exchange(HttpMethod method, String path, Object body, Class<T> responseType) {
        try {
            HttpEntity<Object> entity = new HttpEntity<>(body, headers());
            ResponseEntity<T> response = restTemplate.exchange(baseUrl + path, method, entity, responseType);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new BadRequestException(extractMessage(e));
        } catch (ResourceAccessException e) {
            throw new BadRequestException("The payment service is currently unavailable. Please try again in a moment.");
        }
    }

    private List<PaymentTransactionResponse> exchangeList(String path) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(headers());
            ResponseEntity<PaymentTransactionResponse[]> response =
                    restTemplate.exchange(baseUrl + path, HttpMethod.GET, entity, PaymentTransactionResponse[].class);
            PaymentTransactionResponse[] items = response.getBody();
            return items != null ? Arrays.asList(items) : List.of();
        } catch (HttpStatusCodeException e) {
            throw new BadRequestException(extractMessage(e));
        } catch (ResourceAccessException e) {
            throw new BadRequestException("The payment service is currently unavailable. Please try again in a moment.");
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", internalToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // payment-service's GlobalExceptionHandler always returns {"success": false, "message":
    // "..."} (or {"success": false, "errors": {...}} for validation failures) - pull the
    // human-readable part back out so the guest sees the real reason (e.g. "Rent for
    // 2026-08 has already been paid") instead of a generic "400 Bad Request".
    private String extractMessage(HttpStatusCodeException e) {
        try {
            JsonNode node = objectMapper.readTree(e.getResponseBodyAsString());
            if (node.has("message")) return node.get("message").asText();
            if (node.has("errors")) return node.get("errors").toString();
        } catch (Exception ignored) {
            // fall through to the generic message below
        }
        return "Payment service rejected the request (" + e.getStatusCode() + ").";
    }
}
