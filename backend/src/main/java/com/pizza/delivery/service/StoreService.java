package com.pizza.delivery.service;

import com.pizza.delivery.dto.CategoryDTO;
import com.pizza.delivery.dto.MenuItemDTO;
import com.pizza.delivery.dto.StoreDTO;
import com.pizza.delivery.entity.Store;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.CategoryRepository;
import com.pizza.delivery.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    public List<StoreDTO> getAllStores() {
        return storeRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public StoreDTO getStoreById(String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
        StoreDTO dto = mapToDTO(store);
        dto.setCategories(categoryRepository.findByStoreIdAndIsActiveTrueOrderBySortOrder(storeId).stream()
                .map(cat -> CategoryDTO.builder()
                        .id(cat.getId()).name(cat.getName()).description(cat.getDescription())
                        .imageUrl(cat.getImageUrl()).sortOrder(cat.getSortOrder())
                        .menuItems(cat.getMenuItems().stream().map(mi -> MenuItemDTO.builder()
                                .id(mi.getId()).storeId(mi.getStore().getId())
                                .categoryId(cat.getId()).name(mi.getName())
                                .description(mi.getDescription()).price(mi.getPrice())
                                .imageUrl(mi.getImageUrl()).isAvailable(mi.getIsAvailable())
                                .isPopular(mi.getIsPopular()).calories(mi.getCalories())
                                .preparationTime(mi.getPreparationTime())
                                .customizations(mi.getCustomizations()).tags(mi.getTags())
                                .build()).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList()));
        return dto;
    }

    public List<StoreDTO> getNearbyStores(double lat, double lng, double radiusKm) {
        return storeRepository.findNearbyStores(lat, lng, radiusKm).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private StoreDTO mapToDTO(Store s) {
        return StoreDTO.builder()
                .id(s.getId()).name(s.getName()).description(s.getDescription())
                .phone(s.getPhone()).email(s.getEmail())
                .street(s.getStreet()).city(s.getCity()).state(s.getState())
                .zipCode(s.getZipCode()).country(s.getCountry())
                .lat(s.getLat()).lng(s.getLng()).imageUrl(s.getImageUrl())
                .rating(s.getRating()).reviewCount(s.getReviewCount())
                .isActive(s.getIsActive()).openTime(s.getOpenTime()).closeTime(s.getCloseTime())
                .deliveryRadius(s.getDeliveryRadius()).minOrderAmount(s.getMinOrderAmount())
                .deliveryFee(s.getDeliveryFee()).estimatedDeliveryTime(s.getEstimatedDeliveryTime())
                .build();
    }
}
