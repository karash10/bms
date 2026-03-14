package com.bms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bms.dto.ShowRequest;
import com.bms.entity.Event;
import com.bms.entity.Show;
import com.bms.entity.Venue;
import com.bms.entity_enums.EventType;
import com.bms.exception.ResourceNotFoundException;
import com.bms.repository.EventRepository;
import com.bms.repository.ShowRepository;
import com.bms.repository.VenueRepository;

@ExtendWith(MockitoExtension.class)
class ShowServiceImplTest {

    @Mock
    private ShowRepository showRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private VenueRepository venueRepository;

    private ShowServiceImpl showService;

    @BeforeEach
    void setUp() {
        showService = new ShowServiceImpl(showRepository, eventRepository, venueRepository);
    }

    @Test
    void createShow_shouldCreateAndReturnShow() {
        Event event = createTestEvent(1L, "Test Event");
        Venue venue = createTestVenue(1L, "Test Venue");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        Show savedShow = new Show();
        savedShow.setEvent(event);
        savedShow.setVenue(venue);
        savedShow.setPrice(25.0);
        setId(savedShow, 1L);
        when(showRepository.save(any(Show.class))).thenReturn(savedShow);

        ShowRequest request = new ShowRequest();
        request.setEventId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.of(2026, 4, 1, 14, 0));
        request.setEndTime(LocalDateTime.of(2026, 4, 1, 16, 0));
        request.setPrice(25.0);

        Show result = showService.createShow(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPrice()).isEqualTo(25.0);
        verify(showRepository).save(any(Show.class));
    }

    @Test
    void createShow_whenEventNotFound_shouldThrow() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        ShowRequest request = new ShowRequest();
        request.setEventId(99L);
        request.setVenueId(1L);

        assertThatThrownBy(() -> showService.createShow(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void createShow_whenVenueNotFound_shouldThrow() {
        Event event = createTestEvent(1L, "Event");
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        ShowRequest request = new ShowRequest();
        request.setEventId(1L);
        request.setVenueId(99L);

        assertThatThrownBy(() -> showService.createShow(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue not found");
    }

    @Test
    void getShow_whenExists_shouldReturn() {
        Show show = new Show();
        setId(show, 1L);
        show.setPrice(30.0);
        when(showRepository.findByIdWithEventAndVenue(1L)).thenReturn(Optional.of(show));

        Show result = showService.getShow(1L);

        assertThat(result.getPrice()).isEqualTo(30.0);
    }

    @Test
    void getShow_whenNotFound_shouldThrow() {
        when(showRepository.findByIdWithEventAndVenue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> showService.getShow(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Helpers ---
    private Event createTestEvent(Long id, String title) {
        Event event = new Event();
        event.setTitle(title);
        event.setEventType(EventType.MOVIE);
        setId(event, id);
        return event;
    }

    private Venue createTestVenue(Long id, String name) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setLocation("Test Location");
        setId(venue, id);
        return venue;
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
