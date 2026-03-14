package com.bms.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.entity.Payment;
import com.bms.service.BookingService;
import com.bms.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    public PaymentController(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    @PostMapping("/{bookingId}")
    public Payment pay(@PathVariable Long bookingId){

        return bookingService.processPayment(bookingId);
    }

    
}