package com.bms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bms.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByVenueId(Long venueId);

    /** Count total seats for a venue */
    long countByVenueId(Long venueId);

}