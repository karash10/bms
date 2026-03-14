package com.bms.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bms.entity.Event;
import com.bms.entity.Show;
import com.bms.repository.EventRepository;
import com.bms.repository.ShowRepository;

@Controller
public class PageEventController {

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;

    public PageEventController(EventRepository eventRepository, ShowRepository showRepository) {
        this.eventRepository = eventRepository;
        this.showRepository = showRepository;
    }

    @GetMapping("/events")
    public String listEvents(Model model) {
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "events";
    }

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id, Model model) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        List<Show> shows = showRepository.findByEventIdWithEventAndVenue(id);

        model.addAttribute("event", event);
        model.addAttribute("shows", shows);
        return "event-detail";
    }
}
