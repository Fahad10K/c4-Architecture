package com.pizza.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartDTO {
    private String id;
    private String storeId;
    private String storeName;
    private List<CartItemDTO> items;
    private String couponCode;
    private Double subtotal;
    private Double tax;
    private Double deliveryFee;
    private Double discount;
    private Double total;
}
