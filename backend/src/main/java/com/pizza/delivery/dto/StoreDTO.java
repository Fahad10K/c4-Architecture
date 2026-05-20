package com.pizza.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StoreDTO {
    private String id;
    private String name;
    private String description;
    private String phone;
    private String email;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Double lat;
    private Double lng;
    private String imageUrl;
    private Double rating;
    private Integer reviewCount;
    private Boolean isActive;
    private String openTime;
    private String closeTime;
    private Double deliveryRadius;
    private Double minOrderAmount;
    private Double deliveryFee;
    private Integer estimatedDeliveryTime;
    private List<CategoryDTO> categories;
}
