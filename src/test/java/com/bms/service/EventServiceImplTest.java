package com.bms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bms.dto.EventRequest;
import com.bms.entity.Event;
import com.bms.entity_enums.EventType;
import com.bms.exception.ResourceNotFoundException;
import com.bms.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository);
    }

    @Test
    void createEvent_shouldSaveAndReturnEvent() {
        EventRequest request = new EventRequest();
        request.setTitle("Test Movie");
        request.setDescription("A great movie");
        request.setEventType(EventType.MOVIE);
        request.setDurationMinutes(120);
        request.setImageUrl("http://example.com/img.jpg");

        Event savedEvent = createTestEvent(1L, "Test Movie", EventType.MOVIE);
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        Event result = eventService.createEvent(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Movie");

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event captured = captor.getValue();
        assertThat(captured.getTitle()).isEqualTo("Test Movie");
        assertThat(captured.getEventType()).isEqualTo(EventType.MOVIE);
        assertThat(captured.getDurationMinutes()).isEqualTo(120);
    }

    @Test
    void getEvent_whenExists_shouldReturnEvent() {
        Event event = createTestEvent(1L, "Existing Event", EventType.CONCERT);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventService.getEvent(1L);

        assertThat(result.getTitle()).isEqualTo("Existing Event");
        verify(eventRepository).findById(1L);
    }

    @Test
    void getEvent_whenNotFound_shouldThrowException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found with id 99");
    }

    @Test
    void updateEvent_shouldUpdateFieldsAndSave() {
        Event existing = createTestEvent(1L, "Old Title", EventType.MOVIE);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenReturn(existing);

        EventRequest request = new EventRequest();
        request.setTitle("New Title");
        request.setDescription("Updated desc");
        request.setEventType(EventType.CONCERT);
        request.setDurationMinutes(180);
        request.setImageUrl("http://example.com/new.jpg");

        Event result = eventService.updateEvent(1L, request);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getEventType()).isEqualTo(EventType.CONCERT);
        verify(eventRepository).save(existing);
    }

    @Test
    void updateEvent_whenNotFound_shouldThrowException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        EventRequest request = new EventRequest();
        request.setTitle("Doesn't matter");

        assertThatThrownBy(() -> eventService.updateEvent(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteEvent_shouldCallDeleteById() {
        eventService.deleteEvent(5L);
        verify(eventRepository).deleteById(5L);
    }

    // --- Helper ---
    private Event createTestEvent(Long id, String title, EventType type) {
        Event event = new Event();
        event.setTitle(title);
        event.setEventType(type);
        // id is set by JPA, we use reflection-like approach for testing
        // Since BaseEntity.id has no setter, we use a test subclass or just test title/type
        try {
            var field = event.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(event, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return event;
    }
}
