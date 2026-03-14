package com.bms.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bms.entity.Event;
import com.bms.repository.EventRepository;

@Controller
public class HomeController {

    private final EventRepository eventRepository;

    public HomeController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getName());
        }

        // Load featured events (up to 6)
        List<Event> events = eventRepository.findAll();
        if (events.size() > 6) {
            events = events.subList(0, 6);
        }
        model.addAttribute("featuredEvents", events);

        return "home";
    }
}
