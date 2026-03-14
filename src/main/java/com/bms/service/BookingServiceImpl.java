package com.bms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.dto.BookingRequest;
import com.bms.dto.BookingResponse;
import com.bms.entity.Booking;
import com.bms.entity.BookingSeat;
import com.bms.entity.Payment;
import com.bms.entity.Seat;
import com.bms.entity.Show;
import com.bms.entity.User;
import com.bms.entity_enums.BookingStatus;
import com.bms.entity_enums.PaymentStatus;
import com.bms.exception.ResourceNotFoundException;
import com.bms.exception.SeatAlreadyBookedException;
import com.bms.repository.BookingRepository;
import com.bms.repository.BookingSeatRepository;
import com.bms.repository.SeatRepository;
import com.bms.repository.ShowRepository;
import com.bms.repository.UserRepository;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final PaymentService paymentService;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            UserRepository userRepository,
            ShowRepository showRepository,
            SeatRepository seatRepository,
            PaymentService paymentService) {

        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking: userId={}, showId={}, seatIds={}",
                request.getUserId(), request.getShowId(), request.getSeatIds());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + request.getUserId()));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id " + request.getShowId()));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        // Pessimistic lock: acquire row-level locks on any existing BookingSeat rows
        // for this show + these seats. This blocks concurrent transactions.
        List<BookingSeat> lockedRows = bookingSeatRepository
                .findByShowIdAndSeatIdInForUpdate(show.getId(), request.getSeatIds());

        // If any locked rows exist, those seats are already booked
        if (!lockedRows.isEmpty()) {
            List<Long> alreadyBooked = lockedRows.stream()
                    .map(bs -> bs.getSeat().getId())
                    .toList();
            log.warn("Seat conflict: showId={}, alreadyBooked={}", show.getId(), alreadyBooked);
            throw new SeatAlreadyBookedException(
                    "Seats already booked for this show: " + alreadyBooked);
        }

        // Double-check using a count query (covers edge cases)
        boolean anyBooked = bookingSeatRepository
                .existsByShowIdAndSeatIdIn(show.getId(), request.getSeatIds());

        if (anyBooked) {
            log.warn("Seat conflict (double-check): showId={}, seatIds={}", show.getId(), request.getSeatIds());
            throw new SeatAlreadyBookedException(
                    "One or more seats are already booked for this show");
        }

        // All clear — create the booking as PENDING (awaiting payment)
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);

        booking = bookingRepository.save(booking);

        double totalAmount = 0;
        List<Long> seatIds = new ArrayList<>();

        for (Seat seat : seats) {

            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            bookingSeat.setShow(show);
            bookingSeat.setSeat(seat);
            bookingSeat.setPrice(show.getPrice());

            bookingSeatRepository.save(bookingSeat);

            totalAmount += show.getPrice();
            seatIds.add(seat.getId());
        }

        booking.setTotalAmount(totalAmount);
        bookingRepository.save(booking);

        log.info("Booking created: id={}, status=PENDING, totalAmount={}, seats={}",
                booking.getId(), totalAmount, seatIds.size());

        return new BookingResponse(
                booking.getId(),
                booking.getStatus(),
                totalAmount,
                seatIds
        );
    }

    @Override
    public BookingResponse getBooking(Long bookingId) {
        log.debug("Fetching booking: id={}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(bookingId);

        List<Long> seatIds = new ArrayList<>();

        for (BookingSeat bs : bookingSeats) {
            seatIds.add(bs.getSeat().getId());
        }

        return new BookingResponse(
                booking.getId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                seatIds
        );
    }

    @Override
    public List<BookingResponse> getUserBookings(Long userId) {
        log.debug("Fetching bookings for userId={}", userId);

        List<Booking> bookings = bookingRepository.findByUserId(userId);

        List<BookingResponse> responses = new ArrayList<>();

        for (Booking booking : bookings) {

            List<BookingSeat> bookingSeats =
                    bookingSeatRepository.findByBookingId(booking.getId());

            List<Long> seatIds = new ArrayList<>();

            for (BookingSeat bs : bookingSeats) {
                seatIds.add(bs.getSeat().getId());
            }

            responses.add(
                    new BookingResponse(
                            booking.getId(),
                            booking.getStatus(),
                            booking.getTotalAmount(),
                            seatIds
                    )
            );
        }

        return responses;
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        log.info("Cancelling booking: id={}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Delete BookingSeat rows so those seats become available again
        bookingSeatRepository.deleteByBookingId(bookingId);
        log.info("Booking cancelled and seats released: id={}", bookingId);
    }

    @Override
    @Transactional
    public Payment processPayment(Long bookingId) {
        log.info("Processing payment for booking: id={}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));

        // Only PENDING bookings can be paid
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Payment rejected: booking id={} is not PENDING (status={})", bookingId, booking.getStatus());
            throw new RuntimeException("Booking is not in PENDING state. Current status: " + booking.getStatus());
        }

        // Delegate to PaymentService for simulation
        Payment payment = paymentService.processPayment(booking);

        // Update booking status based on payment result
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            booking.setStatus(BookingStatus.CONFIRMED);
            log.info("Payment SUCCESS for booking: id={}, amount={}", bookingId, payment.getAmount());
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            // Release seats on payment failure
            bookingSeatRepository.deleteByBookingId(bookingId);
            log.warn("Payment FAILED for booking: id={}, seats released", bookingId);
        }

        bookingRepository.save(booking);

        return payment;
    }
}
