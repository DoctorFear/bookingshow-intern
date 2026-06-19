package com.bookingshow.service;

import com.bookingshow.dto.BookingItemRequest;
import com.bookingshow.dto.BookingItemResponse;
import com.bookingshow.dto.BookingRequest;
import com.bookingshow.dto.BookingResponse;
import com.bookingshow.entity.Booking;
import com.bookingshow.entity.BookingItem;
import com.bookingshow.entity.Customer;
import com.bookingshow.entity.TicketType;
import com.bookingshow.enums.BookingStatus;
import com.bookingshow.repository.BookingRepository;
import com.bookingshow.repository.CustomerRepository;
import com.bookingshow.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Booking must have at least one item");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));

        Booking booking = Booking.builder()
                .reference(generateReference())
                .customer(customer)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (BookingItemRequest itemReq : request.getItems()) {
            TicketType ticketType = ticketTypeRepository.findById(itemReq.getTicketTypeId())
                    .orElseThrow(() -> new RuntimeException("TicketType not found: " + itemReq.getTicketTypeId()));

            int requestedQty = itemReq.getQuantity();
            if (requestedQty <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            // Kiểm tra inventory
            if (ticketType.getSoldQuantity() + requestedQty > ticketType.getTotalQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough tickets available for " + ticketType.getName() +
                                ". Remaining: " + (ticketType.getTotalQuantity() - ticketType.getSoldQuantity())
                );
            }

            BookingItem item = BookingItem.builder()
                    .booking(booking)
                    .ticketType(ticketType)
                    .quantity(requestedQty)
                    .unitPrice(ticketType.getPrice())
                    .build();

            booking.getBookingItems().add(item);

            BigDecimal subtotal = ticketType.getPrice().multiply(BigDecimal.valueOf(requestedQty));
            totalAmount = totalAmount.add(subtotal);

            // Update inventory
            ticketType.setSoldQuantity(ticketType.getSoldQuantity() + requestedQty);
        }

        booking.setTotalAmount(totalAmount);
        Booking savedBooking = bookingRepository.save(booking);

        return convertToResponse(savedBooking);
    }

    private String generateReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponse convertToResponse(Booking booking) {
        List<BookingItemResponse> items = booking.getBookingItems().stream()
                .map(item -> BookingItemResponse.builder()
                        .ticketTypeId(item.getTicketType().getId())
                        .ticketTypeName(item.getTicketType().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .reference(booking.getReference())
                .customerId(booking.getCustomer().getId())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .items(items)
                .build();
    }
}