package com.bookingshow.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    private Long customerId;
    private List<BookingItemRequest> items;
}