package com.bookingshow.dto;

import com.bookingshow.enums.Category;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private Category category;
    private String venue;
    private Instant startTime;
    private String organizer;

    // Thêm danh sách ticket types
    private List<TicketTypeResponse> ticketTypes;
}