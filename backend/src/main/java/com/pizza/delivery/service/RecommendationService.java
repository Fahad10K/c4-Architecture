package com.pizza.delivery.service;

import com.pizza.delivery.dto.MenuItemDTO;
import com.pizza.delivery.entity.MenuItem;
import com.pizza.delivery.entity.Order;
import com.pizza.delivery.repository.MenuItemRepository;
import com.pizza.delivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final BedrockRuntimeClient bedrockClient;

    @Value("${aws.bedrock.model-id:anthropic.claude-3-haiku-20240307-v1:0}")
    private String modelId;

    public List<MenuItemDTO> getRecommendations(String userId) {
        List<MenuItem> popular = menuItemRepository.findByIsPopularTrueAndIsAvailableTrue();

        List<String> orderHistory = getUserOrderHistory(userId);

        if (!orderHistory.isEmpty()) {
            return getAIRecommendations(popular, orderHistory);
        }

        return popular.stream()
                .limit(8)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getPersonalizedOffers(String userId) {
        List<MenuItem> items = menuItemRepository.findByIsPopularTrueAndIsAvailableTrue();
        List<String> history = getUserOrderHistory(userId);

        List<MenuItem> recommended = items.stream()
                .sorted((a, b) -> {
                    boolean aOrdered = history.stream().anyMatch(h -> h.contains(a.getName().toLowerCase()));
                    boolean bOrdered = history.stream().anyMatch(h -> h.contains(b.getName().toLowerCase()));
                    if (aOrdered && !bOrdered) return -1;
                    if (!aOrdered && bOrdered) return 1;
                    return Double.compare(b.getPrice(), a.getPrice());
                })
                .limit(4)
                .toList();

        return recommended.stream()
                .map(mi -> MenuItemDTO.builder()
                        .id(mi.getId()).storeId(mi.getStore().getId())
                        .name(mi.getName()).description(mi.getDescription())
                        .price(Math.round(mi.getPrice() * 0.85 * 100.0) / 100.0)
                        .imageUrl(mi.getImageUrl())
                        .isPopular(true).tags(mi.getTags())
                        .build())
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getNextBestItems(String userId, String currentItemId) {
        MenuItem currentItem = menuItemRepository.findById(currentItemId).orElse(null);
        if (currentItem == null) return getRecommendations(userId);

        List<MenuItem> sameCategory = menuItemRepository.findByIsAvailableTrue().stream()
                .filter(mi -> !mi.getId().equals(currentItemId))
                .filter(mi -> mi.getStore().getId().equals(currentItem.getStore().getId()))
                .limit(6)
                .toList();

        return sameCategory.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private List<MenuItemDTO> getAIRecommendations(List<MenuItem> availableItems, List<String> orderHistory) {
        try {
            String itemsList = availableItems.stream()
                    .map(mi -> mi.getName() + " ($" + mi.getPrice() + ") - " + mi.getDescription())
                    .collect(Collectors.joining("\n"));

            String historyStr = String.join(", ", orderHistory);

            String prompt = String.format(
                    "Based on this user's order history: [%s], recommend 6 items from this menu that they would enjoy. " +
                    "Just return the item names separated by newlines, nothing else.\n\nMenu:\n%s",
                    historyStr, itemsList);

            String requestBody = String.format("""
                {"anthropic_version":"bedrock-2023-05-31","max_tokens":200,"messages":[{"role":"user","content":"%s"}]}""",
                    prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromString(requestBody, StandardCharsets.UTF_8))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            Set<String> recommendedNames = parseRecommendedNames(responseBody);

            List<MenuItemDTO> result = availableItems.stream()
                    .filter(mi -> recommendedNames.stream()
                            .anyMatch(name -> mi.getName().toLowerCase().contains(name.toLowerCase())))
                    .limit(8)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            if (result.size() < 4) {
                return availableItems.stream().limit(8).map(this::mapToDTO).collect(Collectors.toList());
            }

            return result;
        } catch (Exception e) {
            log.warn("AI recommendation failed, using popularity fallback: {}", e.getMessage());
            return availableItems.stream().limit(8).map(this::mapToDTO).collect(Collectors.toList());
        }
    }

    private List<String> getUserOrderHistory(String userId) {
        try {
            return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .limit(10)
                    .flatMap(order -> order.getItems().stream())
                    .map(item -> item.getMenuItemName().toLowerCase())
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    private Set<String> parseRecommendedNames(String responseBody) {
        Set<String> names = new HashSet<>();
        try {
            int textIdx = responseBody.indexOf("\"text\"");
            if (textIdx >= 0) {
                int start = responseBody.indexOf("\"", textIdx + 7) + 1;
                int end = responseBody.lastIndexOf("\"");
                if (start > 0 && end > start) {
                    String text = responseBody.substring(start, end)
                            .replace("\\n", "\n").replace("\\\"", "\"");
                    for (String line : text.split("\n")) {
                        String cleaned = line.replaceAll("^[\\d.\\-*]+\\s*", "").trim();
                        if (!cleaned.isEmpty() && cleaned.length() > 2) {
                            names.add(cleaned);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse recommendation response");
        }
        return names;
    }

    private MenuItemDTO mapToDTO(MenuItem mi) {
        return MenuItemDTO.builder()
                .id(mi.getId()).storeId(mi.getStore().getId())
                .name(mi.getName()).description(mi.getDescription())
                .price(mi.getPrice()).imageUrl(mi.getImageUrl())
                .isPopular(mi.getIsPopular()).isAvailable(mi.getIsAvailable())
                .calories(mi.getCalories())
                .customizations(mi.getCustomizations()).tags(mi.getTags())
                .build();
    }
}
