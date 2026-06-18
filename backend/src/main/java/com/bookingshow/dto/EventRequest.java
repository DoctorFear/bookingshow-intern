package com.bookingshow.dto;

import com.bookingshow.enums.Category;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {
    private String title;
    private String description;
    private Category category;
    private String venue;
    private Instant startTime;
    private String organizer;
}