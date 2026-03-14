package com.bms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bms.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}