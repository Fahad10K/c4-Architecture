package com.pizza.delivery.service;

import com.pizza.delivery.dto.MenuItemDTO;
import com.pizza.delivery.dto.StoreDTO;
import com.pizza.delivery.repository.MenuItemRepository;
import com.pizza.delivery.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.kendra.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final MenuItemRepository menuItemRepository;
    private final StoreRepository storeRepository;
    private final KendraClient kendraClient;

    @Value("${aws.kendra.index-id:}")
    private String kendraIndexId;

    public Map<String, Object> globalSearch(String query, int limit) {
        Map<String, Object> results = new HashMap<>();
        results.put("menuItems", searchMenuItems(query, limit));
        results.put("stores", searchStores(query, limit));
        results.put("suggestions", getSearchSuggestions(query));
        return results;
    }

    public List<MenuItemDTO> searchMenuItems(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();

        return menuItemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query)
                .stream()
                .filter(mi -> mi.getIsAvailable())
                .limit(limit > 0 ? limit : 20)
                .map(mi -> MenuItemDTO.builder()
                        .id(mi.getId())
                        .storeId(mi.getStore().getId())
                        .name(mi.getName())
                        .description(mi.getDescription())
                        .price(mi.getPrice())
                        .imageUrl(mi.getImageUrl())
                        .isPopular(mi.getIsPopular())
                        .isAvailable(mi.getIsAvailable())
                        .calories(mi.getCalories())
                        .tags(mi.getTags())
                        .build())
                .collect(Collectors.toList());
    }

    public List<StoreDTO> searchStores(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();

        return storeRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(query, query)
                .stream()
                .filter(s -> s.getIsActive())
                .limit(limit > 0 ? limit : 10)
                .map(s -> StoreDTO.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .address(s.getAddress())
                        .city(s.getCity())
                        .state(s.getState())
                        .phone(s.getPhone())
                        .rating(s.getRating())
                        .reviewCount(s.getReviewCount())
                        .isActive(s.getIsActive())
                        .deliveryFee(s.getDeliveryFee())
                        .minOrderAmount(s.getMinOrderAmount())
                        .estimatedDeliveryTime(s.getEstimatedDeliveryTime())
                        .build())
                .collect(Collectors.toList());
    }

    public List<String> getSearchSuggestions(String query) {
        if (query == null || query.length() < 2) return List.of();

        List<String> suggestions = new ArrayList<>();

        menuItemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query)
                .stream().limit(5)
                .forEach(mi -> suggestions.add(mi.getName()));

        storeRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(query, query)
                .stream().limit(3)
                .forEach(s -> suggestions.add(s.getName()));

        return suggestions.stream().distinct().limit(8).collect(Collectors.toList());
    }

    public Map<String, Object> kendraSearch(String query) {
        if (kendraIndexId == null || kendraIndexId.isEmpty()) {
            log.warn("Kendra index not configured, falling back to database search");
            return globalSearch(query, 20);
        }

        try {
            QueryRequest request = QueryRequest.builder()
                    .indexId(kendraIndexId)
                    .queryText(query)
                    .build();

            QueryResponse response = kendraClient.query(request);

            List<Map<String, Object>> documents = response.resultItems().stream()
                    .map(item -> {
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("id", item.id());
                        doc.put("title", item.documentTitle() != null ? item.documentTitle().text() : "");
                        doc.put("excerpt", item.documentExcerpt() != null ? item.documentExcerpt().text() : "");
                        doc.put("type", item.typeAsString());
                        doc.put("score", item.scoreAttributes() != null ? item.scoreAttributes().scoreConfidenceAsString() : "");
                        return doc;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("documents", documents);
            result.put("totalResults", response.totalNumberOfResults());
            return result;
        } catch (Exception e) {
            log.error("Kendra search failed, falling back to database: {}", e.getMessage());
            return globalSearch(query, 20);
        }
    }

    public void indexMenuItem(String menuItemId) {
        log.info("Indexing menu item {} in OpenSearch/Kendra", menuItemId);
    }

    public void indexStore(String storeId) {
        log.info("Indexing store {} in OpenSearch/Kendra", storeId);
    }
}
