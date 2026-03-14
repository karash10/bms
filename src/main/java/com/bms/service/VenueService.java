package com.bms.service;

import com.bms.dto.VenueRequest;
import com.bms.entity.Venue;

public interface VenueService {

    Venue createVenue(VenueRequest request);

    Venue getVenue(Long id);
}