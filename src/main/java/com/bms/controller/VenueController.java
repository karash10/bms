package com.bms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.dto.VenueRequest;
import com.bms.entity.Venue;
import com.bms.service.VenueService;

@RestController
@RequestMapping("/api/admin/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public Venue createVenue(@RequestBody VenueRequest request) {
        return venueService.createVenue(request);
    }

    @GetMapping("/{id}")
    public Venue getVenue(@PathVariable Long id) {
        return venueService.getVenue(id);
    }
}