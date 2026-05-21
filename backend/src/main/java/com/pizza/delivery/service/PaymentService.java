package com.pizza.delivery.service;

import com.pizza.delivery.dto.PaymentDTO;
import com.pizza.delivery.entity.Order;
import com.pizza.delivery.entity.Payment;
import com.pizza.delivery.enums.OrderStatus;
import com.pizza.delivery.enums.PaymentStatus;
import com.pizza.delivery.exception.BadRequestException;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.OrderRepository;
import com.pizza.delivery.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EventBridgeClient eventBridgeClient;
    private final NotificationService notificationService;

    @Value("${payment.gateway.provider:stripe}")
    private String paymentProvider;

    @Value("${aws.eventbridge.bus-name:pizza-delivery-events}")
    private String eventBusName;

    @Transactional
    public PaymentDTO initiatePayment(String orderId, String paymentMethod, Map<String, String> paymentDetails) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new BadRequestException("Payment already exists for this order");
        }

        String externalPaymentId = processWithGateway(order.getTotal(), order.getId(), paymentMethod, paymentDetails);

        Payment payment = Payment.builder()
                .order(order)
                .stripePaymentId(externalPaymentId)
                .method(paymentMethod)
                .status(PaymentStatus.PROCESSING)
                .amount(order.getTotal())
                .currency("USD")
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment initiated: orderId={}, paymentId={}, amount={}", orderId, payment.getId(), order.getTotal());

        publishPaymentEvent("PaymentInitiated", payment);
        return mapToDTO(payment);
    }

    @Transactional
    public PaymentDTO confirmPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new BadRequestException("Payment cannot be confirmed in status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment = paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        notificationService.sendNotification(
                order.getUser().getId(),
                com.pizza.delivery.enums.NotificationType.ORDER_UPDATE,
                "Payment Confirmed",
                "Payment of $" + payment.getAmount() + " confirmed for order #" + order.getOrderNumber()
        );

        publishPaymentEvent("PaymentCompleted", payment);
        log.info("Payment confirmed: paymentId={}, orderId={}", paymentId, order.getId());
        return mapToDTO(payment);
    }

    @Transactional
    public PaymentDTO processRefund(String paymentId, Double refundAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Can only refund completed payments");
        }

        if (refundAmount == null || refundAmount <= 0) {
            refundAmount = payment.getAmount();
        }

        if (refundAmount > payment.getAmount()) {
            throw new BadRequestException("Refund amount exceeds payment amount");
        }

        processRefundWithGateway(payment.getStripePaymentId(), refundAmount);

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(refundAmount);
        payment = paymentRepository.save(payment);

        notificationService.sendNotification(
                payment.getOrder().getUser().getId(),
                com.pizza.delivery.enums.NotificationType.ORDER_UPDATE,
                "Refund Processed",
                "Refund of $" + refundAmount + " processed for order #" + payment.getOrder().getOrderNumber()
        );

        publishPaymentEvent("PaymentRefunded", payment);
        log.info("Refund processed: paymentId={}, amount={}, reason={}", paymentId, refundAmount, reason);
        return mapToDTO(payment);
    }

    @Transactional
    public PaymentDTO handleWebhook(String provider, Map<String, Object> webhookPayload) {
        log.info("Processing {} webhook: {}", provider, webhookPayload.get("type"));

        String eventType = (String) webhookPayload.get("type");
        String externalId = (String) webhookPayload.getOrDefault("payment_intent_id",
                webhookPayload.get("id"));

        Payment payment = paymentRepository.findByStripePaymentId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "externalId", externalId));

        switch (eventType) {
            case "payment_intent.succeeded":
                payment.setStatus(PaymentStatus.COMPLETED);
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                break;
            case "payment_intent.payment_failed":
                payment.setStatus(PaymentStatus.FAILED);
                break;
            case "charge.refunded":
                payment.setStatus(PaymentStatus.REFUNDED);
                Double refundAmt = ((Number) webhookPayload.getOrDefault("amount_refunded", 0)).doubleValue() / 100.0;
                payment.setRefundAmount(refundAmt);
                break;
            default:
                log.warn("Unhandled webhook event type: {}", eventType);
                break;
        }

        payment = paymentRepository.save(payment);
        return mapToDTO(payment);
    }

    public PaymentDTO getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToDTO(payment);
    }

    public PaymentDTO getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return mapToDTO(payment);
    }

    private String processWithGateway(Double amount, String orderId, String method, Map<String, String> details) {
        log.info("Processing payment with {}: amount={}, orderId={}, method={}", paymentProvider, amount, orderId, method);
        return "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private void processRefundWithGateway(String externalPaymentId, Double refundAmount) {
        log.info("Processing refund with {}: externalId={}, amount={}", paymentProvider, externalPaymentId, refundAmount);
    }

    private void publishPaymentEvent(String eventType, Payment payment) {
        try {
            String detail = String.format(
                    "{\"paymentId\":\"%s\",\"orderId\":\"%s\",\"amount\":%.2f,\"status\":\"%s\"}",
                    payment.getId(), payment.getOrder().getId(), payment.getAmount(), payment.getStatus());

            PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                    .eventBusName(eventBusName)
                    .source("com.pizza.delivery.payment")
                    .detailType(eventType)
                    .detail(detail)
                    .build();

            eventBridgeClient.putEvents(PutEventsRequest.builder().entries(entry).build());
            log.debug("Published event: {} for payment {}", eventType, payment.getId());
        } catch (Exception e) {
            log.warn("Failed to publish payment event: {}", e.getMessage());
        }
    }

    private PaymentDTO mapToDTO(Payment p) {
        return PaymentDTO.builder()
                .id(p.getId())
                .orderId(p.getOrder().getId())
                .stripePaymentId(p.getStripePaymentId())
                .method(p.getMethod())
                .status(p.getStatus())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .refundAmount(p.getRefundAmount())
                .build();
    }
}
