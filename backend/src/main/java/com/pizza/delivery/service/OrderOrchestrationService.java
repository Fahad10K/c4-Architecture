package com.pizza.delivery.service;

import com.pizza.delivery.entity.Order;
import com.pizza.delivery.entity.OrderStatusHistory;
import com.pizza.delivery.enums.DeliveryStatus;
import com.pizza.delivery.enums.NotificationType;
import com.pizza.delivery.enums.OrderStatus;
import com.pizza.delivery.exception.BadRequestException;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.OrderRepository;
import com.pizza.delivery.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOrchestrationService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final EventBridgeClient eventBridgeClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Value("${aws.eventbridge.bus-name:pizza-delivery-events}")
    private String eventBusName;

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PLACED, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.READY, OrderStatus.CANCELLED),
            OrderStatus.READY, Set.of(OrderStatus.PICKED_UP),
            OrderStatus.PICKED_UP, Set.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    @Transactional
    public Order transitionOrderStatus(String orderId, OrderStatus newStatus, String actorId, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus currentStatus = order.getStatus();
        validateTransition(currentStatus, newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        if (newStatus == OrderStatus.DELIVERED) {
            order.setActualDelivery(LocalDateTime.now());
        }

        order = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .changedBy(actorId)
                .notes(notes)
                .build();
        statusHistoryRepository.save(history);

        publishOrderEvent(order, currentStatus, newStatus);
        notifyUser(order, newStatus);
        broadcastOrderUpdate(order);

        log.info("Order {} transitioned: {} -> {} by {}", orderId, currentStatus, newStatus, actorId);
        return order;
    }

    public List<OrderStatus> getAvailableTransitions(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        Set<OrderStatus> transitions = VALID_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        return new ArrayList<>(transitions);
    }

    public List<OrderStatusHistory> getOrderHistory(String orderId) {
        return statusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    @Transactional
    public Order handlePaymentConfirmed(String orderId) {
        return transitionOrderStatus(orderId, OrderStatus.CONFIRMED, "SYSTEM", "Payment confirmed");
    }

    @Transactional
    public Order handleStoreAccepted(String orderId, int prepTimeMinutes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(prepTimeMinutes + 15));
        orderRepository.save(order);
        return transitionOrderStatus(orderId, OrderStatus.PREPARING, "STORE", "Store accepted order");
    }

    @Transactional
    public Order handleOrderReady(String orderId) {
        return transitionOrderStatus(orderId, OrderStatus.READY, "STORE", "Order ready for pickup");
    }

    @Transactional
    public Order handleDriverPickedUp(String orderId, String driverId) {
        return transitionOrderStatus(orderId, OrderStatus.PICKED_UP, driverId, "Driver picked up order");
    }

    @Transactional
    public Order handleOutForDelivery(String orderId, String driverId) {
        return transitionOrderStatus(orderId, OrderStatus.OUT_FOR_DELIVERY, driverId, "Out for delivery");
    }

    @Transactional
    public Order handleDelivered(String orderId, String driverId) {
        return transitionOrderStatus(orderId, OrderStatus.DELIVERED, driverId, "Order delivered");
    }

    @Transactional
    public Order handleCancellation(String orderId, String actorId, String reason) {
        return transitionOrderStatus(orderId, OrderStatus.CANCELLED, actorId, "Cancelled: " + reason);
    }

    private void validateTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> validTargets = VALID_TRANSITIONS.getOrDefault(from, Set.of());
        if (!validTargets.contains(to)) {
            throw new BadRequestException(
                    String.format("Invalid status transition: %s -> %s. Valid transitions: %s", from, to, validTargets));
        }
    }

    private void publishOrderEvent(Order order, OrderStatus from, OrderStatus to) {
        try {
            String detail = String.format(
                    "{\"orderId\":\"%s\",\"orderNumber\":\"%s\",\"userId\":\"%s\",\"fromStatus\":\"%s\",\"toStatus\":\"%s\",\"storeId\":\"%s\",\"timestamp\":\"%s\"}",
                    order.getId(), order.getOrderNumber(), order.getUser().getId(),
                    from, to, order.getStore().getId(), LocalDateTime.now());

            PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                    .eventBusName(eventBusName)
                    .source("com.pizza.delivery.orders")
                    .detailType("OrderStatusChanged")
                    .detail(detail)
                    .build();

            eventBridgeClient.putEvents(PutEventsRequest.builder().entries(entry).build());
        } catch (Exception e) {
            log.warn("Failed to publish order event: {}", e.getMessage());
        }
    }

    private void notifyUser(Order order, OrderStatus newStatus) {
        String title;
        String message;

        switch (newStatus) {
            case CONFIRMED -> { title = "Order Confirmed"; message = "Your order #" + order.getOrderNumber() + " has been confirmed!"; }
            case PREPARING -> { title = "Preparing Your Order"; message = "Your pizza is being prepared!"; }
            case READY -> { title = "Order Ready"; message = "Your order is ready for pickup by the delivery driver."; }
            case PICKED_UP -> { title = "Driver En Route"; message = "Your driver has picked up your order!"; }
            case OUT_FOR_DELIVERY -> { title = "On Its Way!"; message = "Your order is out for delivery. Track your driver in real-time!"; }
            case DELIVERED -> { title = "Order Delivered"; message = "Your order has been delivered. Enjoy your meal!"; }
            case CANCELLED -> { title = "Order Cancelled"; message = "Your order #" + order.getOrderNumber() + " has been cancelled."; }
            default -> { return; }
        }

        notificationService.sendNotification(order.getUser().getId(), NotificationType.ORDER_UPDATE, title, message);
    }

    private void broadcastOrderUpdate(Order order) {
        Map<String, Object> update = Map.of(
                "orderId", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus().name(),
                "updatedAt", LocalDateTime.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/orders/" + order.getId(), update);
        messagingTemplate.convertAndSend("/topic/user-orders/" + order.getUser().getId(), update);
    }
}
