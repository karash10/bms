package com.bms.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.entity.Seat;
import com.bms.entity.Show;
import com.bms.exception.ResourceNotFoundException;
import com.bms.repository.SeatRepository;
import com.bms.repository.ShowRepository;

@RestController
@RequestMapping("/api/shows")
public class PublicShowController {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    public PublicShowController(ShowRepository showRepository,
                                SeatRepository seatRepository) {
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
    }

    @GetMapping("/{id}")
    public Show getShow(@PathVariable Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id " + id));
    }

    @GetMapping("/{id}/seats")
    public List<Seat> getSeats(@PathVariable Long id) {

        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id " + id));

        return seatRepository.findByVenueId(show.getVenue().getId());
    }
}
