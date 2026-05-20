package com.pizza.delivery.service;

import com.pizza.delivery.dto.*;
import com.pizza.delivery.entity.*;
import com.pizza.delivery.enums.DeliveryStatus;
import com.pizza.delivery.enums.OrderStatus;
import com.pizza.delivery.enums.PaymentStatus;
import com.pizza.delivery.exception.BadRequestException;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public OrderDTO createOrder(String userId, String addressId, String specialNotes) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) throw new BadRequestException("Cart is empty");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Store store = cart.getStore();
        Address address = addressId != null ? addressRepository.findById(addressId).orElse(null) : null;

        double subtotal = cart.getItems().stream().mapToDouble(CartItem::getTotalPrice).sum();
        double tax = Math.round(subtotal * 0.08 * 100.0) / 100.0;
        double deliveryFee = store.getDeliveryFee();
        double discount = cart.getDiscount() != null ? cart.getDiscount() : 0;
        double total = Math.round((subtotal + tax + deliveryFee - discount) * 100.0) / 100.0;

        String orderNumber = "PZA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber).user(user).store(store).address(address)
                .status(OrderStatus.PLACED)
                .subtotal(subtotal).tax(tax).deliveryFee(deliveryFee)
                .discount(discount).total(total)
                .couponCode(cart.getCouponCode()).specialNotes(specialNotes)
                .estimatedDelivery(LocalDateTime.now().plusMinutes(store.getEstimatedDeliveryTime()))
                .build();

        order = orderRepository.save(order);

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = OrderItem.builder()
                    .order(order).menuItem(ci.getMenuItem())
                    .name(ci.getMenuItem().getName())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getUnitPrice()).totalPrice(ci.getTotalPrice())
                    .customizations(ci.getCustomizations()).specialNotes(ci.getSpecialNotes())
                    .build();
            order.getItems().add(oi);
        }

        Payment payment = Payment.builder()
                .order(order).amount(total)
                .status(PaymentStatus.COMPLETED)
                .stripePaymentId("sim_" + UUID.randomUUID().toString().substring(0, 12))
                .build();
        order.setPayment(payment);

        Delivery delivery = Delivery.builder()
                .order(order).status(DeliveryStatus.PENDING)
                .pickupLat(store.getLat()).pickupLng(store.getLng())
                .deliveryLat(address != null ? address.getLat() : null)
                .deliveryLng(address != null ? address.getLng() : null)
                .estimatedTime(store.getEstimatedDeliveryTime())
                .build();
        order.setDelivery(delivery);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order).status(OrderStatus.PLACED).note("Order placed").build();
        order.getStatusHistory().add(history);

        order = orderRepository.save(order);
        cartRepository.delete(cart);

        log.info("Order created: {} for user {}", orderNumber, userId);
        return mapToDTO(order);
    }

    public Page<OrderDTO> getUserOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    public OrderDTO getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(status);
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order).status(status).note("Status updated to " + status).build();
        order.getStatusHistory().add(history);
        order = orderRepository.save(order);

        messagingTemplate.convertAndSend("/topic/orders/" + orderId,
                java.util.Map.of("orderId", orderId, "status", status.name()));

        return mapToDTO(order);
    }

    @Transactional
    public OrderDTO cancelOrder(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to cancel this order");
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order cannot be cancelled at this stage");
        }

        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    private OrderDTO mapToDTO(Order o) {
        return OrderDTO.builder()
                .id(o.getId()).orderNumber(o.getOrderNumber())
                .userId(o.getUser().getId()).storeId(o.getStore().getId())
                .storeName(o.getStore().getName())
                .addressId(o.getAddress() != null ? o.getAddress().getId() : null)
                .status(o.getStatus())
                .items(o.getItems().stream().map(oi -> OrderItemDTO.builder()
                        .id(oi.getId()).menuItemId(oi.getMenuItem().getId())
                        .name(oi.getName()).quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice()).totalPrice(oi.getTotalPrice())
                        .customizations(oi.getCustomizations()).specialNotes(oi.getSpecialNotes())
                        .build()).collect(Collectors.toList()))
                .payment(o.getPayment() != null ? PaymentDTO.builder()
                        .id(o.getPayment().getId()).orderId(o.getId())
                        .stripePaymentId(o.getPayment().getStripePaymentId())
                        .method(o.getPayment().getMethod()).status(o.getPayment().getStatus())
                        .amount(o.getPayment().getAmount()).currency(o.getPayment().getCurrency())
                        .build() : null)
                .delivery(o.getDelivery() != null ? DeliveryDTO.builder()
                        .id(o.getDelivery().getId()).orderId(o.getId())
                        .driverId(o.getDelivery().getDriver() != null ? o.getDelivery().getDriver().getId() : null)
                        .driverName(o.getDelivery().getDriver() != null ? o.getDelivery().getDriver().getName() : null)
                        .status(o.getDelivery().getStatus())
                        .currentLat(o.getDelivery().getCurrentLat()).currentLng(o.getDelivery().getCurrentLng())
                        .estimatedTime(o.getDelivery().getEstimatedTime())
                        .build() : null)
                .subtotal(o.getSubtotal()).tax(o.getTax()).deliveryFee(o.getDeliveryFee())
                .discount(o.getDiscount()).total(o.getTotal())
                .couponCode(o.getCouponCode()).specialNotes(o.getSpecialNotes())
                .estimatedDelivery(o.getEstimatedDelivery()).createdAt(o.getCreatedAt())
                .build();
    }
}
