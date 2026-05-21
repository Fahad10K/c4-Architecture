package com.pizza.delivery.controller;

import com.pizza.delivery.dto.DeliveryDTO;
import com.pizza.delivery.enums.DeliveryStatus;
import com.pizza.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryDTO> getDelivery(@PathVariable String orderId) {
        return ResponseEntity.ok(deliveryService.getDeliveryByOrder(orderId));
    }

    @PatchMapping("/{orderId}/location")
    public ResponseEntity<DeliveryDTO> updateLocation(@PathVariable String orderId,
                                                       @RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(deliveryService.updateLocation(orderId, body.get("lat"), body.get("lng")));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<DeliveryDTO> updateStatus(@PathVariable String orderId,
                                                     @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(deliveryService.updateStatus(orderId, DeliveryStatus.valueOf(body.get("status"))));
    }
}
