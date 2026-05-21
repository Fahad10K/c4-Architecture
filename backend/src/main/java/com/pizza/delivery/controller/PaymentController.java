package com.pizza.delivery.controller;

import com.pizza.delivery.dto.PaymentDTO;
import com.pizza.delivery.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentDTO> initiatePayment(@RequestBody Map<String, Object> request) {
        String orderId = (String) request.get("orderId");
        String paymentMethod = (String) request.getOrDefault("method", "card");
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) request.getOrDefault("details", Map.of());
        return ResponseEntity.ok(paymentService.initiatePayment(orderId, paymentMethod, details));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentDTO> confirmPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentId));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<PaymentDTO> processRefund(
            @PathVariable String paymentId,
            @RequestBody Map<String, Object> request) {
        Double amount = request.get("amount") != null ? ((Number) request.get("amount")).doubleValue() : null;
        String reason = (String) request.getOrDefault("reason", "");
        return ResponseEntity.ok(paymentService.processRefund(paymentId, amount, reason));
    }

    @PostMapping("/webhook/{provider}")
    public ResponseEntity<Void> handleWebhook(
            @PathVariable String provider,
            @RequestBody Map<String, Object> payload) {
        paymentService.handleWebhook(provider, payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDTO> getPaymentByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }
}
