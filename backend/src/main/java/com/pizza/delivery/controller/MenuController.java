package com.pizza.delivery.controller;

import com.pizza.delivery.dto.MenuItemDTO;
import com.pizza.delivery.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/stores/{storeId}/menu")
    public ResponseEntity<List<MenuItemDTO>> getStoreMenu(@PathVariable String storeId) {
        return ResponseEntity.ok(menuService.getMenuByStore(storeId));
    }

    @GetMapping("/menu/items/{itemId}")
    public ResponseEntity<MenuItemDTO> getMenuItem(@PathVariable String itemId) {
        return ResponseEntity.ok(menuService.getMenuItem(itemId));
    }

    @GetMapping("/menu/popular")
    public ResponseEntity<List<MenuItemDTO>> getPopularItems() {
        return ResponseEntity.ok(menuService.getPopularItems());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MenuItemDTO>> search(@RequestParam String q) {
        return ResponseEntity.ok(menuService.searchItems(q));
    }
}
