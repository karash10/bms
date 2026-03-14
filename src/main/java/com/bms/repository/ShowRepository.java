package com.bms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bms.entity.Show;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByEventId(Long eventId);

    // --- JOIN FETCH queries for N+1 fix (Phase 9) ---

    @Query("SELECT s FROM Show s JOIN FETCH s.event JOIN FETCH s.venue")
    List<Show> findAllWithEventAndVenue();

    @Query("SELECT s FROM Show s JOIN FETCH s.event JOIN FETCH s.venue WHERE s.id = :id")
    Optional<Show> findByIdWithEventAndVenue(Long id);

    @Query("SELECT s FROM Show s JOIN FETCH s.event JOIN FETCH s.venue WHERE s.event.id = :eventId")
    List<Show> findByEventIdWithEventAndVenue(Long eventId);
}
