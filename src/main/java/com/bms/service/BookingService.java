package com.bms.service;

import java.util.List;

import com.bms.dto.BookingRequest;
import com.bms.dto.BookingResponse;
import com.bms.entity.Payment;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBooking(Long bookingId);

    List<BookingResponse> getUserBookings(Long userId);

    void cancelBooking(Long bookingId);

    Payment processPayment(Long bookingId);
}
