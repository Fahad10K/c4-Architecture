package com.pizza.delivery.service;

import com.pizza.delivery.dto.MenuItemDTO;
import com.pizza.delivery.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MenuItemRepository menuItemRepository;

    public List<MenuItemDTO> getRecommendations(String userId) {
        return menuItemRepository.findByIsPopularTrueAndIsAvailableTrue().stream()
                .limit(8)
                .map(mi -> MenuItemDTO.builder()
                        .id(mi.getId()).storeId(mi.getStore().getId())
                        .name(mi.getName()).description(mi.getDescription())
                        .price(mi.getPrice()).imageUrl(mi.getImageUrl())
                        .isPopular(mi.getIsPopular()).calories(mi.getCalories())
                        .customizations(mi.getCustomizations()).tags(mi.getTags())
                        .build())
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getPersonalizedOffers(String userId) {
        return menuItemRepository.findByIsPopularTrueAndIsAvailableTrue().stream()
                .limit(4)
                .map(mi -> MenuItemDTO.builder()
                        .id(mi.getId()).storeId(mi.getStore().getId())
                        .name(mi.getName()).description(mi.getDescription())
                        .price(mi.getPrice() * 0.9).imageUrl(mi.getImageUrl())
                        .isPopular(true).tags(mi.getTags())
                        .build())
                .collect(Collectors.toList());
    }
}
