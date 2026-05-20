package com.pizza.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItemDTO {
    private String id;
    private String menuItemId;
    private String name;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String customizations;
    private String specialNotes;
}
