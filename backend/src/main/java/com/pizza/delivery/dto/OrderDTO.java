package com.pizza.delivery.dto;

import com.pizza.delivery.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderDTO {
    private String id;
    private String orderNumber;
    private String userId;
    private String storeId;
    private String storeName;
    private String addressId;
    private OrderStatus status;
    private List<OrderItemDTO> items;
    private PaymentDTO payment;
    private DeliveryDTO delivery;
    private Double subtotal;
    private Double tax;
    private Double deliveryFee;
    private Double discount;
    private Double total;
    private String couponCode;
    private String specialNotes;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
}
