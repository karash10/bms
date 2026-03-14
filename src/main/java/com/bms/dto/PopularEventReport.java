package com.bms.dto;

public class PopularEventReport {

    private Long eventId;
    private String eventTitle;
    private Long totalSeatsBooked;

    public PopularEventReport() {
    }

    public PopularEventReport(Long eventId, String eventTitle, Long totalSeatsBooked) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.totalSeatsBooked = totalSeatsBooked;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public Long getTotalSeatsBooked() {
        return totalSeatsBooked;
    }

    public void setTotalSeatsBooked(Long totalSeatsBooked) {
        this.totalSeatsBooked = totalSeatsBooked;
    }
}
