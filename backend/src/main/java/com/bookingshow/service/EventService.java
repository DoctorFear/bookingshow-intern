package com.bookingshow.service;

import com.bookingshow.dto.EventRequest;
import com.bookingshow.dto.EventResponse;
import com.bookingshow.dto.TicketTypeRequest;
import com.bookingshow.dto.TicketTypeResponse;
import com.bookingshow.entity.Event;
import com.bookingshow.entity.TicketType;
import com.bookingshow.repository.EventRepository;
import com.bookingshow.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .venue(request.getVenue())
                .startTime(request.getStartTime())
                .organizer(request.getOrganizer())
                .build();

        Event savedEvent = eventRepository.save(event);
        return convertToEventResponse(savedEvent);
    }

    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAllWithTicketTypes();
        return events.stream()
                .map(this::convertToEventResponse)
                .toList();
    }

    public EventResponse getEventById(Long id) {
        // Sử dụng fetch join để tránh LazyInitializationException
        Event event = eventRepository.findByIdWithTicketTypes(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        return convertToEventResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setVenue(request.getVenue());
        event.setStartTime(request.getStartTime());
        event.setOrganizer(request.getOrganizer());

        Event updated = eventRepository.save(event);
        return convertToEventResponse(updated);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        eventRepository.delete(event);
    }

    // ==================== TICKET TYPE ====================

    @Transactional
    public TicketTypeResponse addTicketType(Long eventId, TicketTypeRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (request.getTotalQuantity() <= 0) {
            throw new IllegalArgumentException("Total quantity must be greater than 0");
        }

        TicketType ticketType = TicketType.builder()
                .event(event)
                .name(request.getName())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .soldQuantity(0)
                .build();

        TicketType saved = ticketTypeRepository.save(ticketType);
        return convertToTicketTypeResponse(saved);
    }

    public List<TicketTypeResponse> getTicketTypesByEvent(Long eventId) {
        return ticketTypeRepository.findByEventId(eventId).stream()
                .map(this::convertToTicketTypeResponse)
                .toList();
    }

    @Transactional
    public TicketTypeResponse updateTicketType(Long eventId, Long ticketTypeId, TicketTypeRequest request) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new RuntimeException("TicketType not found"));

        if (!ticketType.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("TicketType does not belong to this event");
        }

        ticketType.setName(request.getName());
        ticketType.setPrice(request.getPrice());
        if (request.getTotalQuantity() != null && request.getTotalQuantity() > 0) {
            ticketType.setTotalQuantity(request.getTotalQuantity());
        }

        TicketType updated = ticketTypeRepository.save(ticketType);
        return convertToTicketTypeResponse(updated);
    }

    @Transactional
    public void deleteTicketType(Long eventId, Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new RuntimeException("TicketType not found"));

        if (!ticketType.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("TicketType does not belong to this event");
        }

        ticketTypeRepository.delete(ticketType);
    }

    // ==================== CONVERTER ====================

    private EventResponse convertToEventResponse(Event event) {
        List<TicketTypeResponse> ticketResponses = new ArrayList<>();

        if (event.getTicketTypes() != null) {
            ticketResponses = event.getTicketTypes().stream()
                    .map(this::convertToTicketTypeResponse)
                    .toList();
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .venue(event.getVenue())
                .startTime(event.getStartTime())
                .organizer(event.getOrganizer())
                .ticketTypes(ticketResponses)
                .build();
    }

    private TicketTypeResponse convertToTicketTypeResponse(TicketType tt) {
        return TicketTypeResponse.builder()
                .id(tt.getId())
                .eventId(tt.getEvent().getId())
                .name(tt.getName())
                .price(tt.getPrice())
                .totalQuantity(tt.getTotalQuantity())
                .soldQuantity(tt.getSoldQuantity())
                .remainingQuantity(tt.getTotalQuantity() - tt.getSoldQuantity())
                .build();
    }
}