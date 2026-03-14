package com.bms.service;

import com.bms.dto.EventRequest;
import com.bms.entity.Event;

public interface EventService {

    Event createEvent(EventRequest request);

    Event updateEvent(Long id, EventRequest request);

    void deleteEvent(Long id);

    Event getEvent(Long id);
}