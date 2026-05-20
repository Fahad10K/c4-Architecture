package com.pizza.delivery.service;

import com.pizza.delivery.dto.MenuItemDTO;
import com.pizza.delivery.entity.MenuItem;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public List<MenuItemDTO> getMenuByStore(String storeId) {
        return menuItemRepository.findByStoreIdAndIsAvailableTrue(storeId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public MenuItemDTO getMenuItem(String itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));
        return mapToDTO(item);
    }

    public List<MenuItemDTO> getPopularItems() {
        return menuItemRepository.findByIsPopularTrueAndIsAvailableTrue().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<MenuItemDTO> searchItems(String query) {
        return menuItemRepository.searchItems(query).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    private MenuItemDTO mapToDTO(MenuItem mi) {
        return MenuItemDTO.builder()
                .id(mi.getId())
                .storeId(mi.getStore().getId())
                .categoryId(mi.getCategory() != null ? mi.getCategory().getId() : null)
                .name(mi.getName()).description(mi.getDescription())
                .price(mi.getPrice()).imageUrl(mi.getImageUrl())
                .isAvailable(mi.getIsAvailable()).isPopular(mi.getIsPopular())
                .calories(mi.getCalories()).preparationTime(mi.getPreparationTime())
                .customizations(mi.getCustomizations()).tags(mi.getTags())
                .build();
    }
}
