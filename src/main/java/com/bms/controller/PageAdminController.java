package com.bms.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bms.dto.EventRequest;
import com.bms.dto.ShowRequest;
import com.bms.dto.VenueRequest;
import com.bms.entity.Event;
import com.bms.entity.Show;
import com.bms.entity.Venue;
import com.bms.entity_enums.EventType;
import com.bms.repository.EventRepository;
import com.bms.repository.ShowRepository;
import com.bms.repository.VenueRepository;
import com.bms.service.EventService;
import com.bms.service.ReportService;
import com.bms.service.ShowService;
import com.bms.service.VenueService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class PageAdminController {

    private final EventService eventService;
    private final VenueService venueService;
    private final ShowService showService;
    private final ReportService reportService;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ShowRepository showRepository;

    public PageAdminController(
            EventService eventService,
            VenueService venueService,
            ShowService showService,
            ReportService reportService,
            EventRepository eventRepository,
            VenueRepository venueRepository,
            ShowRepository showRepository) {
        this.eventService = eventService;
        this.venueService = venueService;
        this.showService = showService;
        this.reportService = reportService;
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.showRepository = showRepository;
    }

    // ========== Dashboard ==========

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("eventCount", eventRepository.count());
        model.addAttribute("venueCount", venueRepository.count());
        model.addAttribute("showCount", showRepository.count());
        return "admin/dashboard";
    }

    // ========== Events ==========

    @GetMapping("/events")
    public String eventsPage(Model model) {
        List<Event> events = eventRepository.findAll();
        model.addAttribute("events", events);
        return "admin/events";
    }

    @PostMapping("/events/create")
    public String createEvent(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam EventType eventType,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) String imageUrl,
            RedirectAttributes redirectAttributes) {

        try {
            EventRequest request = new EventRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setEventType(eventType);
            request.setDurationMinutes(durationMinutes);
            request.setImageUrl(imageUrl);

            eventService.createEvent(request);
            redirectAttributes.addFlashAttribute("success", "Event created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create event: " + e.getMessage());
        }

        return "redirect:/admin/events";
    }

    @PostMapping("/events/delete")
    public String deleteEvent(@RequestParam Long eventId, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(eventId);
            redirectAttributes.addFlashAttribute("success", "Event deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    // ========== Venues ==========

    @GetMapping("/venues")
    public String venuesPage(Model model) {
        List<Venue> venues = venueRepository.findAll();
        model.addAttribute("venues", venues);
        return "admin/venues";
    }

    @PostMapping("/venues/create")
    public String createVenue(
            @RequestParam String name,
            @RequestParam String location,
            @RequestParam Integer rows,
            @RequestParam Integer seatsPerRow,
            RedirectAttributes redirectAttributes) {

        try {
            VenueRequest request = new VenueRequest();
            request.setName(name);
            request.setLocation(location);
            request.setRows(rows);
            request.setSeatsPerRow(seatsPerRow);

            venueService.createVenue(request);
            redirectAttributes.addFlashAttribute("success",
                    "Venue created with " + (rows * seatsPerRow) + " seats!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create venue: " + e.getMessage());
        }

        return "redirect:/admin/venues";
    }

    // ========== Shows ==========

    @GetMapping("/shows")
    public String showsPage(Model model) {
        List<Event> events = eventRepository.findAll();
        List<Venue> venues = venueRepository.findAll();
        List<Show> shows = showRepository.findAllWithEventAndVenue();

        model.addAttribute("events", events);
        model.addAttribute("venues", venues);
        model.addAttribute("shows", shows);
        return "admin/shows";
    }

    @PostMapping("/shows/create")
    public String createShow(
            @RequestParam Long eventId,
            @RequestParam Long venueId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam Double price,
            RedirectAttributes redirectAttributes) {

        try {
            ShowRequest request = new ShowRequest();
            request.setEventId(eventId);
            request.setVenueId(venueId);
            request.setStartTime(LocalDateTime.parse(startTime));
            request.setEndTime(LocalDateTime.parse(endTime));
            request.setPrice(price);

            showService.createShow(request);
            redirectAttributes.addFlashAttribute("success", "Show created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create show: " + e.getMessage());
        }

        return "redirect:/admin/shows";
    }

    // ========== Reports ==========

    @GetMapping("/reports")
    public String reportsPage(Model model) {
        model.addAttribute("totalBookings", reportService.getTotalBookings());
        model.addAttribute("totalRevenue", reportService.getTotalRevenue());
        model.addAttribute("bookingsPerEvent", reportService.getBookingsPerEvent());
        model.addAttribute("revenuePerEvent", reportService.getRevenuePerEvent());
        model.addAttribute("popularEvents", reportService.getPopularEvents());
        model.addAttribute("seatOccupancy", reportService.getSeatOccupancy());
        return "admin/reports";
    }
}
