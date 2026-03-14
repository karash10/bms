package com.bms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bms.dto.VenueRequest;
import com.bms.entity.Seat;
import com.bms.entity.Venue;
import com.bms.entity_enums.SeatType;
import com.bms.exception.ResourceNotFoundException;
import com.bms.repository.SeatRepository;
import com.bms.repository.VenueRepository;

@Service
public class VenueServiceImpl implements VenueService {

    private static final Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    public VenueServiceImpl(VenueRepository venueRepository,
                            SeatRepository seatRepository) {
        this.venueRepository = venueRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public Venue createVenue(VenueRequest request) {
        log.info("Creating venue: name={}, location={}, rows={}, seatsPerRow={}",
                request.getName(), request.getLocation(), request.getRows(), request.getSeatsPerRow());

        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setLocation(request.getLocation());

        venue = venueRepository.save(venue);

        generateSeats(venue, request.getRows(), request.getSeatsPerRow());

        log.info("Venue created: id={}, name={}, totalSeats={}",
                venue.getId(), venue.getName(), request.getRows() * request.getSeatsPerRow());
        return venue;
    }

    private void generateSeats(Venue venue, int rows, int seatsPerRow) {

        for (int i = 0; i < rows; i++) {

            char rowChar = (char) ('A' + i);

            for (int j = 1; j <= seatsPerRow; j++) {

                Seat seat = new Seat();
                seat.setSeatRow(String.valueOf(rowChar));
                seat.setSeatNumber(j);
                seat.setSeatType(SeatType.REGULAR);
                seat.setVenue(venue);

                seatRepository.save(seat);
            }
        }
    }

    @Override
    public Venue getVenue(Long id) {
        log.debug("Fetching venue: id={}", id);
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id " + id));
    }
}
