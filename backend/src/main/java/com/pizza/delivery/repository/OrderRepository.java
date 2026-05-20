package com.pizza.delivery.repository;

import com.pizza.delivery.entity.Order;
import com.pizza.delivery.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<Order> findByStoreIdAndStatus(String storeId, OrderStatus status);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatus(OrderStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByStoreIdOrderByCreatedAtDesc(String storeId);
}
