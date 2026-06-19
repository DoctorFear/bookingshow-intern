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

    Page<Event> findAll(Pageable pageable);

    // Tìm kiếm nâng cao + Pagination
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.ticketTypes WHERE " +
            "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR e.category = :category) " +
            "AND (:startDate IS NULL OR e.startTime >= :startDate) " +
            "AND (:endDate IS NULL OR e.startTime <= :endDate)")
    Page<Event> searchEvents(
            @Param("keyword") String keyword,
            @Param("category") Category category,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);


    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByKeyword(String keyword);


    // ==================== FETCH JOIN ====================
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.ticketTypes WHERE e.id = :id")
    Optional<Event> findByIdWithTicketTypes(@Param("id") Long id);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.ticketTypes")
    List<Event> findAllWithTicketTypes();

}