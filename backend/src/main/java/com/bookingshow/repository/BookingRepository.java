package com.bookingshow.repository;

import com.bookingshow.entity.Booking;
import com.bookingshow.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    Optional<Booking> findByReference(String reference);

    List<Booking> findByStatus(BookingStatus status);

    // Fetch join để load bookingItems
    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.customer " +
            "LEFT JOIN FETCH b.bookingItems bi " +
            "LEFT JOIN FETCH bi.ticketType tt " +
            "LEFT JOIN FETCH tt.event " +
            "WHERE b.id = :id")
    Optional<Booking> findByIdWithItems(@Param("id") Long id);
}