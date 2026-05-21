package com.pizza.delivery.controller;

import com.pizza.delivery.dto.DeliveryDTO;
import com.pizza.delivery.entity.Order;
import com.pizza.delivery.service.DeliveryService;
import com.pizza.delivery.service.OrderOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/driver")
@PreAuthorize("hasAnyRole('DRIVER', 'DELIVERY_PARTNER', 'ADMIN')")
@RequiredArgsConstructor
public class DriverController {

    private final DeliveryService deliveryService;
    private final OrderOrchestrationService orchestrationService;

    @GetMapping("/deliveries/available")
    public ResponseEntity<?> getAvailableDeliveries() {
        return ResponseEntity.ok(deliveryService.getAvailableDeliveries());
    }

    @GetMapping("/deliveries/active")
    public ResponseEntity<?> getActiveDeliveries(@RequestParam String driverId) {
        return ResponseEntity.ok(deliveryService.getDriverActiveDeliveries(driverId));
    }

    @PostMapping("/deliveries/{orderId}/pickup")
    public ResponseEntity<Map<String, Object>> pickupOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        String driverId = request.get("driverId");
        Order order = orchestrationService.handleDriverPickedUp(orderId, driverId);
        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().name(),
                "message", "Order picked up by driver"
        ));
    }

    @PostMapping("/deliveries/{orderId}/out-for-delivery")
    public ResponseEntity<Map<String, Object>> outForDelivery(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        String driverId = request.get("driverId");
        Order order = orchestrationService.handleOutForDelivery(orderId, driverId);
        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().name(),
                "message", "Order out for delivery"
        ));
    }

    @PostMapping("/deliveries/{orderId}/delivered")
    public ResponseEntity<Map<String, Object>> markDelivered(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        String driverId = request.get("driverId");
        Order order = orchestrationService.handleDelivered(orderId, driverId);
        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().name(),
                "message", "Order delivered successfully"
        ));
    }

    @PostMapping("/deliveries/{deliveryId}/location")
    public ResponseEntity<DeliveryDTO> updateLocation(
            @PathVariable String deliveryId,
            @RequestBody Map<String, Double> location) {
        Double lat = location.get("latitude");
        Double lng = location.get("longitude");
        return ResponseEntity.ok(deliveryService.updateDriverLocation(deliveryId, lat, lng));
    }

    @GetMapping("/deliveries/{deliveryId}/tracking")
    public ResponseEntity<DeliveryDTO> getDeliveryTracking(@PathVariable String deliveryId) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(deliveryId));
    }
}
