package com.bms.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bms.entity.Booking;
import com.bms.entity.Payment;
import com.bms.entity_enums.PaymentStatus;
import com.bms.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(Booking booking) {
        log.info("Processing payment: bookingId={}, amount={}", booking.getId(), booking.getTotalAmount());

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentTime(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        simulateGatewayDelay();

        boolean paymentResult = simulatePayment();

        if (paymentResult) {
            payment.setStatus(PaymentStatus.SUCCESS);
            log.info("Payment succeeded: paymentId={}, bookingId={}", payment.getId(), booking.getId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Payment failed: paymentId={}, bookingId={}", payment.getId(), booking.getId());
        }

        return paymentRepository.save(payment);
    }

    private boolean simulatePayment() {
        Random random = new Random();
        return random.nextInt(10) < 8; // 80% success rate
    }

    private void simulateGatewayDelay() {
        try {
            Thread.sleep(2000); // 2 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
