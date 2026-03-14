package com.bms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.bms.dto.ShowRequest;
import com.bms.entity.Event;
import com.bms.entity.Show;
import com.bms.entity.Venue;
import com.bms.exception.ResourceNotFoundException;
import com.bms.repository.EventRepository;
import com.bms.repository.ShowRepository;
import com.bms.repository.VenueRepository;

@Service
public class ShowServiceImpl implements ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowServiceImpl.class);

    private final ShowRepository showRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    public ShowServiceImpl(ShowRepository showRepository,
                           EventRepository eventRepository,
                           VenueRepository venueRepository) {
        this.showRepository = showRepository;
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    @Override
    @CacheEvict(value = "shows", allEntries = true)
    public Show createShow(ShowRequest request) {
        log.info("Creating show: eventId={}, venueId={}, startTime={}",
                request.getEventId(), request.getVenueId(), request.getStartTime());

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id " + request.getEventId()));
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venue not found with id " + request.getVenueId()));

        Show show = new Show();
        show.setEvent(event);
        show.setVenue(venue);
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        show.setPrice(request.getPrice());

        Show saved = showRepository.save(show);
        log.info("Show created: id={}, event={}, venue={}", saved.getId(),
                event.getTitle(), venue.getName());
        return saved;
    }

    @Override
    @Cacheable(value = "shows", key = "#id")
    public Show getShow(Long id) {
        log.info("Fetching show from DB: id={}", id);
        return showRepository.findByIdWithEventAndVenue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id " + id));
    }
}
