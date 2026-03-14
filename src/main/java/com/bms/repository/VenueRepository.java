package com.bms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bms.entity.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}