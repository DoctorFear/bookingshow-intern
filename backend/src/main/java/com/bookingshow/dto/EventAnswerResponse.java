package com.bookingshow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAnswerResponse {
    private String question;
    private String answer;
    private Long eventId;
}