package com.bms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bms.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    // --- Analytics queries ---

    /** Total number of confirmed bookings */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    long countConfirmedBookings();

    /** Total revenue from confirmed bookings */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    double sumConfirmedRevenue();

    /** Bookings per event (confirmed only) */
    @Query("SELECT b.show.event.id, b.show.event.title, COUNT(b) " +
           "FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "GROUP BY b.show.event.id, b.show.event.title " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> countBookingsPerEvent();

    /** Revenue per event (confirmed only) */
    @Query("SELECT b.show.event.id, b.show.event.title, COALESCE(SUM(b.totalAmount), 0) " +
           "FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "GROUP BY b.show.event.id, b.show.event.title " +
           "ORDER BY SUM(b.totalAmount) DESC")
    List<Object[]> sumRevenuePerEvent();

    // --- JOIN FETCH queries for N+1 fix (Phase 9) ---

    @Query("SELECT b FROM Booking b JOIN FETCH b.show s JOIN FETCH s.event JOIN FETCH b.user WHERE b.user.id = :userId")
    List<Booking> findByUserIdWithShowAndEvent(Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.show s JOIN FETCH s.event JOIN FETCH b.user WHERE b.id = :id")
    java.util.Optional<Booking> findByIdWithShowAndEvent(Long id);
}
