package com.bms.dto;

public class RevenueReport {

    private Long eventId;
    private String eventTitle;
    private Double totalRevenue;

    public RevenueReport() {
    }

    public RevenueReport(Long eventId, String eventTitle, Double totalRevenue) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.totalRevenue = totalRevenue;
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

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
