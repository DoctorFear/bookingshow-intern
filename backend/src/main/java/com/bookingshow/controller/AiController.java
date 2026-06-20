package com.bookingshow.controller;

import com.bookingshow.dto.EventAnswerResponse;
import com.bookingshow.dto.EventQuestionRequest;
import com.bookingshow.dto.EventResponse;
import com.bookingshow.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/search")
    public ResponseEntity<List<EventResponse>> search(@RequestBody AiSearchQuery query) {
        List<EventResponse> results = aiService.searchByNaturalLanguage(query.getQuery());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/events/{id}/ask")
    public ResponseEntity<EventAnswerResponse> askAboutEvent(
            @PathVariable Long id,
            @RequestBody EventQuestionRequest request) {

        EventAnswerResponse answer = aiService.answerEventQuestion(id, request.getQuestion());
        return ResponseEntity.ok(answer);
    }
}

// DTO đơn giản cho input
class AiSearchQuery {
    private String query;
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}