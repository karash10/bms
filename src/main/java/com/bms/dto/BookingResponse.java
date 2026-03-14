package com.bms.dto;

import java.util.List;

import com.bms.entity_enums.BookingStatus;

public class BookingResponse {

    private Long bookingId;
    private BookingStatus status;
    private Double totalAmount;
    private List<Long> seatIds;

    public BookingResponse(Long bookingId,
                           BookingStatus status,
                           Double totalAmount,
                           List<Long> seatIds) {
        this.bookingId = bookingId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.seatIds = seatIds;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }
}