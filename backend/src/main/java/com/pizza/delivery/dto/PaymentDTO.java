package com.pizza.delivery.dto;

import com.pizza.delivery.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentDTO {
    private String id;
    private String orderId;
    private String stripePaymentId;
    private String method;
    private PaymentStatus status;
    private Double amount;
    private String currency;
    private Double refundAmount;
}
