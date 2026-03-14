package com.bms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.bms.dto.EventRequest;
import com.bms.entity.Event;
import com.bms.exception.ResourceNotFoundException;
import com.bms.repository.EventRepository;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @CacheEvict(value = "events", allEntries = true)
    public Event createEvent(EventRequest request) {
        log.info("Creating event: title={}, type={}", request.getTitle(), request.getEventType());

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setDurationMinutes(request.getDurationMinutes());
        event.setImageUrl(request.getImageUrl());

        Event saved = eventRepository.save(event);
        log.info("Event created: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    @Override
    @CacheEvict(value = "events", allEntries = true)
    public Event updateEvent(Long id, EventRequest request) {
        log.info("Updating event: id={}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + id));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setDurationMinutes(request.getDurationMinutes());
        event.setImageUrl(request.getImageUrl());

        Event saved = eventRepository.save(event);
        log.info("Event updated: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    @Override
    @CacheEvict(value = "events", allEntries = true)
    public void deleteEvent(Long id) {
        log.info("Deleting event: id={}", id);
        eventRepository.deleteById(id);
        log.info("Event deleted: id={}", id);
    }

    @Override
    @Cacheable(value = "events", key = "#id")
    public Event getEvent(Long id) {
        log.info("Fetching event from DB: id={}", id);
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + id));
    }
}
