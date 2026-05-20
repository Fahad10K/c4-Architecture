package com.pizza.delivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItemDTO {
    private String id;
    @NotBlank
    private String menuItemId;
    private String menuItemName;
    private String menuItemImage;
    @Min(1)
    private Integer quantity;
    private String customizations;
    private Double unitPrice;
    private Double totalPrice;
    private String specialNotes;
}
