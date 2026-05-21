package com.pizza.delivery.repository;

import com.pizza.delivery.entity.Delivery;
import com.pizza.delivery.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String> {
    Optional<Delivery> findByOrderId(String orderId);
    List<Delivery> findByDriverId(String driverId);
    List<Delivery> findByDriverIdAndStatus(String driverId, DeliveryStatus status);
    List<Delivery> findByDriverIdAndStatusIn(String driverId, List<DeliveryStatus> statuses);
    List<Delivery> findByStatus(DeliveryStatus status);
}
