package com.bookingshow.repository;

import com.bookingshow.entity.Event;
import com.bookingshow.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategory(Category category);

    List<Event> findByStartTimeBetween(Instant start, Instant end);

    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByKeyword(String keyword);

    Page<Event> findAll(Pageable pageable);

    // ==================== FETCH JOIN ====================
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.ticketTypes WHERE e.id = :id")
    Optional<Event> findByIdWithTicketTypes(@Param("id") Long id);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.ticketTypes")
    List<Event> findAllWithTicketTypes();
}