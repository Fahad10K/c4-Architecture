package com.pizza.delivery.service;

import com.pizza.delivery.dto.DeliveryDTO;
import com.pizza.delivery.entity.Delivery;
import com.pizza.delivery.enums.DeliveryStatus;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DeliveryDTO getDeliveryByOrder(String orderId) {
        Delivery d = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));
        return mapToDTO(d);
    }

    @Transactional
    public DeliveryDTO updateLocation(String orderId, Double lat, Double lng) {
        Delivery d = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));
        d.setCurrentLat(lat);
        d.setCurrentLng(lng);
        d = deliveryRepository.save(d);

        messagingTemplate.convertAndSend("/topic/delivery/" + orderId,
                Map.of("orderId", orderId, "lat", lat, "lng", lng, "status", d.getStatus().name()));
        return mapToDTO(d);
    }

    @Transactional
    public DeliveryDTO updateStatus(String orderId, DeliveryStatus status) {
        Delivery d = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));
        d.setStatus(status);
        if (status == DeliveryStatus.PICKED_UP) d.setPickedUpAt(LocalDateTime.now());
        if (status == DeliveryStatus.DELIVERED) d.setDeliveredAt(LocalDateTime.now());
        d = deliveryRepository.save(d);

        messagingTemplate.convertAndSend("/topic/delivery/" + orderId,
                Map.of("orderId", orderId, "status", status.name()));
        return mapToDTO(d);
    }

    private DeliveryDTO mapToDTO(Delivery d) {
        return DeliveryDTO.builder()
                .id(d.getId()).orderId(d.getOrder().getId())
                .driverId(d.getDriver() != null ? d.getDriver().getId() : null)
                .driverName(d.getDriver() != null ? d.getDriver().getName() : null)
                .status(d.getStatus())
                .currentLat(d.getCurrentLat()).currentLng(d.getCurrentLng())
                .estimatedTime(d.getEstimatedTime()).distance(d.getDistance())
                .assignedAt(d.getAssignedAt()).deliveredAt(d.getDeliveredAt())
                .build();
    }
}
