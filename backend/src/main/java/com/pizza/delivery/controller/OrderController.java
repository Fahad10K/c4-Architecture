package com.pizza.delivery.controller;

import com.pizza.delivery.dto.OrderDTO;
import com.pizza.delivery.enums.OrderStatus;
import com.pizza.delivery.security.UserPrincipal;
import com.pizza.delivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@AuthenticationPrincipal UserPrincipal principal,
                                                 @RequestBody Map<String, String> body) {
        OrderDTO order = orderService.createOrder(
                principal.getId(), body.get("addressId"), body.get("specialNotes"));
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getUserOrders(@AuthenticationPrincipal UserPrincipal principal,
                                                         Pageable pageable) {
        return ResponseEntity.ok(orderService.getUserOrders(principal.getId(), pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateStatus(@PathVariable String orderId,
                                                  @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, OrderStatus.valueOf(body.get("status"))));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, principal.getId()));
    }
}
