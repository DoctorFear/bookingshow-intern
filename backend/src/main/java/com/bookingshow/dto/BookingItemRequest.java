package com.bookingshow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItemRequest {
    private Long ticketTypeId;
    private Integer quantity;
}