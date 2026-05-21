package com.pizza.delivery.controller;

import com.pizza.delivery.entity.Order;
import com.pizza.delivery.enums.OrderStatus;
import com.pizza.delivery.service.MenuService;
import com.pizza.delivery.service.OrderOrchestrationService;
import com.pizza.delivery.service.OrderService;
import com.pizza.delivery.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff")
@PreAuthorize("hasAnyRole('STAFF', 'STORE_STAFF', 'ADMIN')")
@RequiredArgsConstructor
public class StaffController {

    private final OrderOrchestrationService orchestrationService;
    private final OrderService orderService;
    private final StoreService storeService;
    private final MenuService menuService;

    @GetMapping("/store/{storeId}/orders")
    public ResponseEntity<List<?>> getStoreOrders(
            @PathVariable String storeId,
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.getStoreOrders(storeId, status));
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<Map<String, Object>> acceptOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, Integer> request) {
        int prepTime = request.getOrDefault("prepTimeMinutes", 20);
        Order order = orchestrationService.handleStoreAccepted(orderId, prepTime);
        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().name(),
                "message", "Order accepted"
        ));
    }

    @PostMapping("/orders/{orderId}/ready")
    public ResponseEntity<Map<String, Object>> markOrderReady(@PathVariable String orderId) {
        Order order = orchestrationService.handleOrderReady(orderId);
        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().name(),
                "message", "Order marked as ready"
        ));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "Cancelled by store staff");
        Order order = orchestrationService.handleCancellation(orderId, "STORE_STAFF", reason);
        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().name(),
                "message", "Order cancelled"
        ));
    }

    @GetMapping("/orders/{orderId}/transitions")
    public ResponseEntity<List<OrderStatus>> getAvailableTransitions(@PathVariable String orderId) {
        return ResponseEntity.ok(orchestrationService.getAvailableTransitions(orderId));
    }

    @GetMapping("/orders/{orderId}/history")
    public ResponseEntity<?> getOrderHistory(@PathVariable String orderId) {
        return ResponseEntity.ok(orchestrationService.getOrderHistory(orderId));
    }
}
