package com.bms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "booking_seat",
    uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_id"})
)
public class BookingSeat extends BaseEntity {

    @ManyToOne
    private Booking booking;

    @ManyToOne
    private Show show;

    @ManyToOne
    private Seat seat;

    private Double price;
}
