package com.bms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bms.entity.Booking;
import com.bms.entity.Payment;
import com.bms.entity_enums.BookingStatus;
import com.bms.entity_enums.PaymentStatus;
import com.bms.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository);
    }

    @Test
    void processPayment_shouldCreatePaymentAndSetStatus() {
        Booking booking = new Booking();
        booking.setTotalAmount(100.0);
        booking.setStatus(BookingStatus.PENDING);
        setId(booking, 1L);

        // Return the argument itself so in-place status changes are reflected
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.processPayment(booking);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(100.0);
        // Status should be either SUCCESS or FAILED (80% success rate)
        assertThat(result.getStatus()).isIn(PaymentStatus.SUCCESS, PaymentStatus.FAILED);
        // save is called twice: once to create PENDING, once to update final status
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    private void setId(Object entity, Long id) {
        try {
            var clazz = entity.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, id);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
