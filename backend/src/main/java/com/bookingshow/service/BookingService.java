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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bookingshow.util.PdfGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PdfGenerator pdfGenerator;

    // ====================== CREATE BOOKING (PENDING) ======================
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingResponse createBooking(BookingRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Booking booking = Booking.builder()
                .reference(generateReference())
                .customer(customer)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (BookingItemRequest itemReq : request.getItems()) {
            TicketType ticketType = ticketTypeRepository.findByIdWithPessimisticLock(itemReq.getTicketTypeId())
                    .orElseThrow(() -> new RuntimeException("TicketType not found"));

            int requestedQty = itemReq.getQuantity();
            int available = ticketType.getTotalQuantity() - ticketType.getSoldQuantity();

            if (available < requestedQty) {
                throw new IllegalArgumentException(
                        String.format("Không đủ vé cho %s. Còn lại: %d, Yêu cầu: %d",
                                ticketType.getName(), available, requestedQty)
                );
            }

            BookingItem item = BookingItem.builder()
                    .booking(booking)
                    .ticketType(ticketType)
                    .quantity(requestedQty)
                    .unitPrice(ticketType.getPrice())
                    .build();

            booking.getBookingItems().add(item);

            ticketType.setSoldQuantity(ticketType.getSoldQuantity() + requestedQty);
            totalAmount = totalAmount.add(ticketType.getPrice().multiply(BigDecimal.valueOf(requestedQty)));
        }

        booking.setTotalAmount(totalAmount);
        Booking saved = bookingRepository.save(booking);
        return convertToResponse(saved);
    }

    // ====================== PAY (Simulated) ======================
    @Transactional
    public BookingResponse payBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ booking ở trạng thái PENDING mới được thanh toán");
        }

        booking.setStatus(BookingStatus.PAID);
        // Có thể tự động CONFIRMED luôn (tùy business)
        // booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);
        return convertToResponse(saved);
    }

    // ====================== CONFIRM ======================
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PAID) {
            throw new IllegalArgumentException("Chỉ booking PAID mới được confirm");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        return convertToResponse(saved);
    }

    // ====================== CANCEL + RELEASE INVENTORY ======================
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Chỉ release inventory nếu chưa confirm hoặc đã paid
        if (booking.getStatus() == BookingStatus.PAID || booking.getStatus() == BookingStatus.CONFIRMED) {
            for (BookingItem item : booking.getBookingItems()) {
                TicketType ticketType = item.getTicketType();
                ticketType.setSoldQuantity(ticketType.getSoldQuantity() - item.getQuantity());
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    // ====================== HELPER METHODS ======================
    private String generateReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponse convertToResponse(Booking booking) {
        List<BookingItemResponse> items = new ArrayList<>();

        if (booking.getBookingItems() != null) {
            items = booking.getBookingItems().stream()
                    .map(item -> {
                        TicketType tt = item.getTicketType();
                        return BookingItemResponse.builder()
                                .ticketTypeId(tt.getId())
                                .ticketTypeName(tt.getName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .build();
                    })
                    .toList();
        }

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

    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return convertToResponse(booking);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> generateConfirmationPdf(Long bookingId) {
        // Fetch full data với join fetch
        Booking booking = bookingRepository.findByIdWithItems(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only CONFIRMED bookings can generate PDF confirmation");
        }

        // Force initialize lazy collections
        if (booking.getCustomer() != null) {
            booking.getCustomer().getName(); // trigger load
        }
        if (booking.getBookingItems() != null) {
            booking.getBookingItems().size(); // trigger load
            booking.getBookingItems().forEach(item -> {
                if (item.getTicketType() != null) {
                    item.getTicketType().getName();
                    if (item.getTicketType().getEvent() != null) {
                        item.getTicketType().getEvent().getTitle();
                    }
                }
            });
        }

        byte[] pdfBytes = pdfGenerator.generateBookingConfirmation(booking);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "booking-" + booking.getReference() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}