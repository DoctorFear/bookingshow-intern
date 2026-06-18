package com.bookingshow.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeRequest {
    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
}