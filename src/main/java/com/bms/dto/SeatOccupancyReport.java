package com.bms.dto;

public class SeatOccupancyReport {

    private Long showId;
    private String eventTitle;
    private String venueName;
    private Long totalSeats;
    private Long bookedSeats;
    private Double occupancyPercent;

    public SeatOccupancyReport() {
    }

    public SeatOccupancyReport(Long showId, String eventTitle, String venueName,
                                Long totalSeats, Long bookedSeats, Double occupancyPercent) {
        this.showId = showId;
        this.eventTitle = eventTitle;
        this.venueName = venueName;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
        this.occupancyPercent = occupancyPercent;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public Long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Long totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Long getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(Long bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public Double getOccupancyPercent() {
        return occupancyPercent;
    }

    public void setOccupancyPercent(Double occupancyPercent) {
        this.occupancyPercent = occupancyPercent;
    }
}
