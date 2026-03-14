package com.bms.dto;

import com.bms.entity_enums.EventType;

public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private EventType eventType;
    private Integer durationMinutes;
    private String imageUrl;

    public EventResponse(Long id, String title, String description,
                         EventType eventType, Integer durationMinutes, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.durationMinutes = durationMinutes;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public EventType getEventType() { return eventType; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getImageUrl() { return imageUrl; }
}