package com.bms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.entity.BookingSeat;

import jakarta.persistence.LockModeType;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    List<BookingSeat> findByBookingId(Long bookingId);

    /**
     * Pessimistic lock: selects existing BookingSeat rows for the given show + seats.
     * This locks the rows so concurrent transactions block until this one commits.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bs FROM BookingSeat bs WHERE bs.show.id = :showId AND bs.seat.id IN :seatIds")
    List<BookingSeat> findByShowIdAndSeatIdInForUpdate(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds);

    /**
     * Check if any of the given seats are already booked for a show.
     */
    @Query("SELECT CASE WHEN COUNT(bs) > 0 THEN true ELSE false END FROM BookingSeat bs WHERE bs.show.id = :showId AND bs.seat.id IN :seatIds")
    boolean existsByShowIdAndSeatIdIn(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds);

    void deleteByBookingId(Long bookingId);

    /**
     * Find all booked seats for a given show (read-only, no lock).
     */
    List<BookingSeat> findByShowId(Long showId);

    /** Count booked seats per show (for occupancy report) */
    long countByShowId(Long showId);

    /** Most popular events by total seats booked (confirmed bookings only) */
    @Query("SELECT bs.show.event.id, bs.show.event.title, COUNT(bs) " +
           "FROM BookingSeat bs WHERE bs.booking.status = 'CONFIRMED' " +
           "GROUP BY bs.show.event.id, bs.show.event.title " +
           "ORDER BY COUNT(bs) DESC")
    List<Object[]> countSeatsBookedPerEvent();
}
