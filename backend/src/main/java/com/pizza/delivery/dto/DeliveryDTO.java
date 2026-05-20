package com.pizza.delivery.dto;

import com.pizza.delivery.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryDTO {
    private String id;
    private String orderId;
    private String driverId;
    private String driverName;
    private DeliveryStatus status;
    private Double currentLat;
    private Double currentLng;
    private Integer estimatedTime;
    private Double distance;
    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;
}
