package com.bms.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.entity.Event;
import com.bms.entity.Show;
import com.bms.exception.ResourceNotFoundException;
import com.bms.repository.EventRepository;
import com.bms.repository.ShowRepository;

@RestController
@RequestMapping("/api/events")
public class PublicEventController {

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;

    public PublicEventController(EventRepository eventRepository,
                                 ShowRepository showRepository) {
        this.eventRepository = eventRepository;
        this.showRepository = showRepository;
    }

    @GetMapping
    public Page<Event> getEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + id));
    }

    @GetMapping("/{id}/shows")
    public List<Show> getShowsForEvent(@PathVariable Long id) {
        return showRepository.findByEventId(id);
    }
}
