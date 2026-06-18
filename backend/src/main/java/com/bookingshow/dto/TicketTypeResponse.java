package com.bookingshow.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeResponse {
    private Long id;
    private Long eventId;
    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer soldQuantity;
    private Integer remainingQuantity;
}