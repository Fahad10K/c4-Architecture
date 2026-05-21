package com.pizza.delivery.service;

import com.pizza.delivery.dto.DeliveryDTO;
import com.pizza.delivery.entity.Delivery;
import com.pizza.delivery.enums.DeliveryStatus;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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

    public DeliveryDTO getDeliveryById(String deliveryId) {
        Delivery d = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));
        return mapToDTO(d);
    }

    public List<DeliveryDTO> getAvailableDeliveries() {
        return deliveryRepository.findByStatus(DeliveryStatus.PENDING).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryDTO> getDriverActiveDeliveries(String driverId) {
        return deliveryRepository.findByDriverIdAndStatusIn(driverId,
                List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT))
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public DeliveryDTO updateDriverLocation(String deliveryId, Double lat, Double lng) {
        Delivery d = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));
        d.setCurrentLat(lat);
        d.setCurrentLng(lng);
        d = deliveryRepository.save(d);

        String orderId = d.getOrder().getId();
        messagingTemplate.convertAndSend("/topic/delivery/" + orderId,
                Map.of("deliveryId", deliveryId, "orderId", orderId,
                        "lat", lat, "lng", lng, "status", d.getStatus().name()));
        log.debug("Driver location updated: delivery={}, lat={}, lng={}", deliveryId, lat, lng);
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

    @Transactional
    public DeliveryDTO assignDriver(String deliveryId, String driverId) {
        Delivery d = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));
        d.setStatus(DeliveryStatus.ASSIGNED);
        d.setAssignedAt(LocalDateTime.now());
        d = deliveryRepository.save(d);

        log.info("Delivery {} assigned to driver {}", deliveryId, driverId);
        return mapToDTO(d);
    }

    public DeliveryDTO calculateETA(String deliveryId) {
        Delivery d = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        if (d.getCurrentLat() != null && d.getDeliveryLat() != null) {
            double distKm = haversineDistance(
                    d.getCurrentLat(), d.getCurrentLng(),
                    d.getDeliveryLat(), d.getDeliveryLng());
            int etaMinutes = (int) Math.ceil(distKm / 0.5);
            d.setEstimatedTime(etaMinutes);
            d.setDistance(Math.round(distKm * 10.0) / 10.0);
            d = deliveryRepository.save(d);
        }

        return mapToDTO(d);
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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
