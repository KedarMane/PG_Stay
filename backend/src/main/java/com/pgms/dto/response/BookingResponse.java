package com.pgms.dto.response;

import com.pgms.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private UserResponse guest;
    private BedSummaryResponse bed;
    private LocalDate checkInDate;
    private LocalDateTime leftAt;
    private Integer planDurationMonths;
    private BigDecimal discountPercent;
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;

    public static BookingResponse from(Booking b) {
        if (b == null) return null;
        return new BookingResponse(
                b.getId(), UserResponse.from(b.getGuest()), BedSummaryResponse.from(b.getBed()),
                b.getCheckInDate(), b.getLeftAt(),
                b.getPlanDurationMonths(), b.getDiscountPercent(),
                b.getStatus() != null ? b.getStatus().name() : null,
                b.getRejectionReason(), b.getCreatedAt()
        );
    }
}
