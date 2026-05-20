package com.pizza.delivery.controller;

import com.pizza.delivery.dto.CartDTO;
import com.pizza.delivery.dto.CartItemDTO;
import com.pizza.delivery.security.UserPrincipal;
import com.pizza.delivery.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(@AuthenticationPrincipal UserPrincipal principal,
                                            @RequestBody CartItemDTO item) {
        return ResponseEntity.ok(cartService.addItem(principal.getId(), item));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItem(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable String itemId,
                                               @RequestBody CartItemDTO item) {
        return ResponseEntity.ok(cartService.updateItem(principal.getId(), itemId, item));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItem(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable String itemId) {
        return ResponseEntity.ok(cartService.removeItem(principal.getId(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<CartDTO> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.clearCart(principal.getId()));
    }

    @PostMapping("/apply-coupon")
    public ResponseEntity<CartDTO> applyCoupon(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(cartService.applyCoupon(principal.getId(), body.get("couponCode")));
    }
}
