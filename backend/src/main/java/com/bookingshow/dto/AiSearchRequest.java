package com.bookingshow.dto;

import com.bookingshow.enums.Category;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSearchRequest {
    private String keyword;
    private Category category;
    private Instant startDate;
    private Instant endDate;
}