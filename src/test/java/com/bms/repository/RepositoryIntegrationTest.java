package com.bms.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.bms.entity.Booking;
import com.bms.entity.BookingSeat;
import com.bms.entity.Event;
import com.bms.entity.Seat;
import com.bms.entity.Show;
import com.bms.entity.User;
import com.bms.entity.Venue;
import com.bms.entity_enums.BookingStatus;
import com.bms.entity_enums.EventType;
import com.bms.entity_enums.SeatType;
import com.bms.entity_enums.UserRole;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired private TestEntityManager em;
    @Autowired private EventRepository eventRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookingSeatRepository bookingSeatRepository;
    @Autowired private SeatRepository seatRepository;

    private Event event;
    private Venue venue;
    private Show show;
    private User user;

    @BeforeEach
    void setUp() {
        // Create test entities
        event = new Event();
        event.setTitle("Test Event");
        event.setEventType(EventType.MOVIE);
        event.setDurationMinutes(120);
        em.persist(event);

        venue = new Venue();
        venue.setName("Test Venue");
        venue.setLocation("Test Location");
        em.persist(venue);

        // Create seats for the venue
        for (int i = 1; i <= 5; i++) {
            Seat seat = new Seat();
            seat.setSeatRow("A");
            seat.setSeatNumber(i);
            seat.setSeatType(SeatType.REGULAR);
            seat.setVenue(venue);
            em.persist(seat);
        }

        show = new Show();
        show.setEvent(event);
        show.setVenue(venue);
        show.setStartTime(LocalDateTime.now().plusDays(1));
        show.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        show.setPrice(25.0);
        em.persist(show);

        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        user.setRole(UserRole.USER);
        em.persist(user);

        em.flush();
    }

    // --- ShowRepository tests ---

    @Test
    void findAllWithEventAndVenue_shouldFetchAssociations() {
        List<Show> shows = showRepository.findAllWithEventAndVenue();

        assertThat(shows).hasSize(1);
        assertThat(shows.get(0).getEvent().getTitle()).isEqualTo("Test Event");
        assertThat(shows.get(0).getVenue().getName()).isEqualTo("Test Venue");
    }

    @Test
    void findByIdWithEventAndVenue_shouldFetchAssociations() {
        var result = showRepository.findByIdWithEventAndVenue(show.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getEvent().getTitle()).isEqualTo("Test Event");
        assertThat(result.get().getVenue().getName()).isEqualTo("Test Venue");
    }

    @Test
    void findByEventIdWithEventAndVenue_shouldReturnShowsForEvent() {
        List<Show> shows = showRepository.findByEventIdWithEventAndVenue(event.getId());

        assertThat(shows).hasSize(1);
        assertThat(shows.get(0).getPrice()).isEqualTo(25.0);
    }

    // --- SeatRepository tests ---

    @Test
    void countByVenueId_shouldReturnSeatCount() {
        long count = seatRepository.countByVenueId(venue.getId());
        assertThat(count).isEqualTo(5);
    }

    @Test
    void findByVenueId_shouldReturnAllSeats() {
        List<Seat> seats = seatRepository.findByVenueId(venue.getId());
        assertThat(seats).hasSize(5);
    }

    // --- BookingRepository tests ---

    @Test
    void countConfirmedBookings_shouldCountOnlyConfirmed() {
        createBooking(BookingStatus.CONFIRMED, 50.0);
        createBooking(BookingStatus.PENDING, 25.0);
        createBooking(BookingStatus.CANCELLED, 30.0);
        em.flush();

        long count = bookingRepository.countConfirmedBookings();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void sumConfirmedRevenue_shouldSumOnlyConfirmed() {
        createBooking(BookingStatus.CONFIRMED, 50.0);
        createBooking(BookingStatus.CONFIRMED, 100.0);
        createBooking(BookingStatus.CANCELLED, 999.0);
        em.flush();

        double revenue = bookingRepository.sumConfirmedRevenue();
        assertThat(revenue).isEqualTo(150.0);
    }

    @Test
    void findByUserIdWithShowAndEvent_shouldFetchAssociations() {
        createBooking(BookingStatus.CONFIRMED, 50.0);
        em.flush();

        List<Booking> bookings = bookingRepository.findByUserIdWithShowAndEvent(user.getId());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getShow().getEvent().getTitle()).isEqualTo("Test Event");
    }

    // --- BookingSeatRepository tests ---

    @Test
    void countByShowId_shouldCountBookedSeats() {
        Booking booking = createBooking(BookingStatus.CONFIRMED, 50.0);
        List<Seat> seats = seatRepository.findByVenueId(venue.getId());

        BookingSeat bs = new BookingSeat();
        bs.setBooking(booking);
        bs.setShow(show);
        bs.setSeat(seats.get(0));
        bs.setPrice(25.0);
        em.persist(bs);
        em.flush();

        long count = bookingSeatRepository.countByShowId(show.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    void existsByShowIdAndSeatIdIn_shouldDetectBookedSeats() {
        Booking booking = createBooking(BookingStatus.CONFIRMED, 25.0);
        List<Seat> seats = seatRepository.findByVenueId(venue.getId());

        BookingSeat bs = new BookingSeat();
        bs.setBooking(booking);
        bs.setShow(show);
        bs.setSeat(seats.get(0));
        bs.setPrice(25.0);
        em.persist(bs);
        em.flush();

        boolean exists = bookingSeatRepository.existsByShowIdAndSeatIdIn(
                show.getId(), List.of(seats.get(0).getId()));
        assertThat(exists).isTrue();

        boolean notExists = bookingSeatRepository.existsByShowIdAndSeatIdIn(
                show.getId(), List.of(seats.get(4).getId()));
        assertThat(notExists).isFalse();
    }

    // --- Helper ---
    private Booking createBooking(BookingStatus status, double amount) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(status);
        booking.setTotalAmount(amount);
        em.persist(booking);
        return booking;
    }
}
