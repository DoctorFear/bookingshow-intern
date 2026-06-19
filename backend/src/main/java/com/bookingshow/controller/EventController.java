package com.bookingshow.controller;

import com.bookingshow.dto.EventRequest;
import com.bookingshow.dto.EventResponse;
import com.bookingshow.dto.TicketTypeRequest;
import com.bookingshow.dto.TicketTypeResponse;
import com.bookingshow.enums.Category;
import com.bookingshow.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(    "/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== TICKET TYPES ====================

    @PostMapping("/{eventId}/ticket-types")
    public ResponseEntity<TicketTypeResponse> addTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketTypeRequest request) {
        return ResponseEntity.ok(eventService.addTicketType(eventId, request));
    }

    @GetMapping("/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypes(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getTicketTypesByEvent(eventId));
    }

    @PutMapping("/{eventId}/ticket-types/{ticketTypeId}")
    public ResponseEntity<TicketTypeResponse> updateTicketType(
            @PathVariable Long eventId,
            @PathVariable Long ticketTypeId,
            @Valid @RequestBody TicketTypeRequest request) {
        return ResponseEntity.ok(eventService.updateTicketType(eventId, ticketTypeId, request));
    }

    @DeleteMapping("/{eventId}/ticket-types/{ticketTypeId}")
    public ResponseEntity<Void> deleteTicketType(
            @PathVariable Long eventId,
            @PathVariable Long ticketTypeId) {
        eventService.deleteTicketType(eventId, ticketTypeId);
        return ResponseEntity.noContent().build();
    }


    // Search & Filter
    @GetMapping("/search")
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        Page<EventResponse> result = eventService.searchEvents(keyword, category, startDate, endDate, pageable);
        return ResponseEntity.ok(result);
    }
}