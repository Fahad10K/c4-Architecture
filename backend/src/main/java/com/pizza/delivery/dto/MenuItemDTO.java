package com.pizza.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuItemDTO {
    private String id;
    private String storeId;
    private String categoryId;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isPopular;
    private Integer calories;
    private Integer preparationTime;
    private String customizations;
    private String tags;
}
