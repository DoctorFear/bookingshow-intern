package com.bookingshow.controller;

import com.bookingshow.dto.BookingRequest;
import com.bookingshow.dto.BookingResponse;
import com.bookingshow.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // Tạo booking (PENDING)
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    // Thanh toán (Simulated)
    @PostMapping("/{id}/pay")
    public ResponseEntity<BookingResponse> payBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.payBooking(id));
    }

    // Xác nhận booking
    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    // Hủy booking
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // Lấy thông tin booking
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/{id}/confirmation")
    public ResponseEntity<byte[]> getBookingConfirmation(@PathVariable Long id) {
        return bookingService.generateConfirmationPdf(id);
    }
}