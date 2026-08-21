package com.pgms.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// A separate deployable Spring Boot app - its own JAR, own port (8081), own database.
// Run it with: mvn spring-boot:run (from the payment-service/ folder), alongside the main
// backend on 8080. See README.md in this folder for the full run-book.
@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
