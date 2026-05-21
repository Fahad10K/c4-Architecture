package com.pizza.delivery.controller;

import com.pizza.delivery.dto.MenuItemDTO;
import com.pizza.delivery.security.UserPrincipal;
import com.pizza.delivery.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<MenuItemDTO>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recommendationService.getRecommendations(principal.getId()));
    }

    @GetMapping("/offers")
    public ResponseEntity<List<MenuItemDTO>> getOffers(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recommendationService.getPersonalizedOffers(principal.getId()));
    }
}
