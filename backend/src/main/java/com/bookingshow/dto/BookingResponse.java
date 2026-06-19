package com.bookingshow.dto;

import com.bookingshow.enums.BookingStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private String reference;
    private Long customerId;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<BookingItemResponse> items;
}