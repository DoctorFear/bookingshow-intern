package com.bookingshow.repository;

import com.bookingshow.entity.Booking;
import com.bookingshow.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    Optional<Booking> findByReference(String reference);

    List<Booking> findByStatus(BookingStatus status);
}