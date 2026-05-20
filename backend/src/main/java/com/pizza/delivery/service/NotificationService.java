package com.pizza.delivery.service;

import com.pizza.delivery.dto.NotificationDTO;
import com.pizza.delivery.entity.Notification;
import com.pizza.delivery.entity.User;
import com.pizza.delivery.enums.NotificationType;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.NotificationRepository;
import com.pizza.delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Page<NotificationDTO> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationDTO markAsRead(String notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        n.setIsRead(true);
        n = notificationRepository.save(n);
        return mapToDTO(n);
    }

    @Transactional
    public void sendNotification(String userId, NotificationType type, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = Notification.builder()
                .user(user).type(type).title(title).message(message).build();
        notification = notificationRepository.save(notification);

        messagingTemplate.convertAndSend("/topic/notifications/" + userId, mapToDTO(notification));
        log.info("Notification sent to user {}: {}", userId, title);
    }

    private NotificationDTO mapToDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId()).type(n.getType()).title(n.getTitle())
                .message(n.getMessage()).data(n.getData())
                .isRead(n.getIsRead()).createdAt(n.getCreatedAt())
                .build();
    }
}
