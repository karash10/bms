package com.bms.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
    @Index(name = "idx_show_start_time", columnList = "startTime"),
    @Index(name = "idx_show_event_id", columnList = "event_id"),
    @Index(name = "idx_show_venue_id", columnList = "venue_id")
})
public class Show extends BaseEntity {

    @ManyToOne
    @JsonBackReference
    private Event event;

    @ManyToOne
    private Venue venue;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double price;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}