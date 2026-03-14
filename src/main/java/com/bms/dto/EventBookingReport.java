package com.bms.dto;

public class EventBookingReport {

    private Long eventId;
    private String eventTitle;
    private Long bookingCount;

    public EventBookingReport() {
    }

    public EventBookingReport(Long eventId, String eventTitle, Long bookingCount) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.bookingCount = bookingCount;
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

    public Long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(Long bookingCount) {
        this.bookingCount = bookingCount;
    }
}
