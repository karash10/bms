package com.bms.entity;

import java.time.LocalDateTime;

import com.bms.entity_enums.BookingStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = {
    @Index(name = "idx_booking_user_id", columnList = "user_id"),
    @Index(name = "idx_booking_show_id", columnList = "show_id"),
    @Index(name = "idx_booking_status", columnList = "status")
})
public class Booking extends BaseEntity {

    @ManyToOne
    private User user;

    @ManyToOne
    private Show show;

    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Double totalAmount;
}
