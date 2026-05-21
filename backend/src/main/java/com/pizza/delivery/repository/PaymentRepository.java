package com.pizza.delivery.repository;

import com.pizza.delivery.entity.Payment;
import com.pizza.delivery.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByStripePaymentId(String stripePaymentId);
    List<Payment> findByStatus(PaymentStatus status);
}
