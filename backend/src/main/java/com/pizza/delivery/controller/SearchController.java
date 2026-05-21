package com.pizza.delivery.controller;

import com.pizza.delivery.dto.MenuItemDTO;
import com.pizza.delivery.dto.StoreDTO;
import com.pizza.delivery.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> globalSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.globalSearch(q, limit));
    }

    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemDTO>> searchMenu(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.searchMenuItems(q, limit));
    }

    @GetMapping("/stores")
    public ResponseEntity<List<StoreDTO>> searchStores(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.searchStores(q, limit));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String q) {
        return ResponseEntity.ok(searchService.getSearchSuggestions(q));
    }

    @GetMapping("/knowledge")
    public ResponseEntity<Map<String, Object>> kendraSearch(@RequestParam String q) {
        return ResponseEntity.ok(searchService.kendraSearch(q));
    }
}
