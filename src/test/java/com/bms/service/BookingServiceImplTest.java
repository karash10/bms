package com.bms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bms.dto.BookingRequest;
import com.bms.dto.BookingResponse;
import com.bms.entity.Booking;
import com.bms.entity.BookingSeat;
import com.bms.entity.Seat;
import com.bms.entity.Show;
import com.bms.entity.User;
import com.bms.entity_enums.BookingStatus;
import com.bms.entity_enums.SeatType;
import com.bms.exception.ResourceNotFoundException;
import com.bms.exception.SeatAlreadyBookedException;
import com.bms.repository.BookingRepository;
import com.bms.repository.BookingSeatRepository;
import com.bms.repository.SeatRepository;
import com.bms.repository.ShowRepository;
import com.bms.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;
    @Mock private UserRepository userRepository;
    @Mock private ShowRepository showRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private PaymentService paymentService;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository, bookingSeatRepository,
                userRepository, showRepository,
                seatRepository, paymentService);
    }

    @Test
    void createBooking_shouldCreatePendingBooking() {
        User user = createUser(1L, "test@example.com");
        Show show = createShow(1L, 25.0);
        Seat seat1 = createSeat(1L);
        Seat seat2 = createSeat(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showRepository.findById(1L)).thenReturn(Optional.of(show));
        when(seatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(seat1, seat2));
        when(bookingSeatRepository.findByShowIdAndSeatIdInForUpdate(1L, List.of(1L, 2L)))
                .thenReturn(new ArrayList<>());
        when(bookingSeatRepository.existsByShowIdAndSeatIdIn(1L, List.of(1L, 2L)))
                .thenReturn(false);

        Booking savedBooking = new Booking();
        savedBooking.setStatus(BookingStatus.PENDING);
        setId(savedBooking, 1L);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setShowId(1L);
        request.setSeatIds(List.of(1L, 2L));

        BookingResponse response = bookingService.createBooking(request);

        assertThat(response.getBookingId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void createBooking_whenSeatsAlreadyBooked_shouldThrow() {
        User user = createUser(1L, "test@example.com");
        Show show = createShow(1L, 25.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showRepository.findById(1L)).thenReturn(Optional.of(show));

        // Simulate locked rows exist (seat already booked)
        BookingSeat existingBs = new BookingSeat();
        Seat seat = createSeat(1L);
        existingBs.setSeat(seat);
        when(bookingSeatRepository.findByShowIdAndSeatIdInForUpdate(1L, List.of(1L)))
                .thenReturn(List.of(existingBs));

        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setShowId(1L);
        request.setSeatIds(List.of(1L));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(SeatAlreadyBookedException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenUserNotFound_shouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BookingRequest request = new BookingRequest();
        request.setUserId(99L);
        request.setShowId(1L);
        request.setSeatIds(List.of(1L));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getBooking_shouldReturnBookingResponse() {
        Booking booking = new Booking();
        setId(booking, 1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(50.0);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingSeat bs = new BookingSeat();
        Seat seat = createSeat(5L);
        bs.setSeat(seat);
        when(bookingSeatRepository.findByBookingId(1L)).thenReturn(List.of(bs));

        BookingResponse response = bookingService.getBooking(1L);

        assertThat(response.getBookingId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.getSeatIds()).containsExactly(5L);
    }

    @Test
    void cancelBooking_shouldSetCancelledAndDeleteSeats() {
        Booking booking = new Booking();
        setId(booking, 1L);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
        verify(bookingSeatRepository).deleteByBookingId(1L);
    }

    @Test
    void cancelBooking_whenNotFound_shouldThrow() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processPayment_whenNotPending_shouldThrow() {
        Booking booking = new Booking();
        setId(booking, 1L);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.processPayment(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not in PENDING state");
    }

    // --- Helpers ---
    private User createUser(Long id, String email) {
        User user = new User();
        user.setEmail(email);
        user.setName("Test User");
        setId(user, id);
        return user;
    }

    private Show createShow(Long id, Double price) {
        Show show = new Show();
        show.setPrice(price);
        show.setStartTime(LocalDateTime.now().plusDays(1));
        setId(show, id);
        return show;
    }

    private Seat createSeat(Long id) {
        Seat seat = new Seat();
        seat.setSeatRow("A");
        seat.setSeatNumber(1);
        seat.setSeatType(SeatType.REGULAR);
        setId(seat, id);
        return seat;
    }

    private void setId(Object entity, Long id) {
        try {
            var clazz = entity.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, id);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
