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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SnsClient snsClient;
    private final SesClient sesClient;

    @Value("${aws.ses.from-email:noreply@pizzapalace.com}")
    private String fromEmail;

    @Value("${aws.sns.sms-enabled:false}")
    private boolean smsEnabled;

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
    public void markAllAsRead(String userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(n -> { n.setIsRead(true); notificationRepository.save(n); });
    }

    @Transactional
    public void sendNotification(String userId, NotificationType type, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = Notification.builder()
                .user(user).type(type).title(title).message(message).build();
        notification = notificationRepository.save(notification);

        messagingTemplate.convertAndSend("/topic/notifications/" + userId, mapToDTO(notification));

        if (type == NotificationType.ORDER_UPDATE || type == NotificationType.PROMOTION) {
            sendEmailNotification(user.getEmail(), title, message);
        }

        if (smsEnabled && user.getPhone() != null && !user.getPhone().isBlank()) {
            if (type == NotificationType.ORDER_UPDATE) {
                sendSmsNotification(user.getPhone(), title + ": " + message);
            }
        }

        log.info("Notification sent to user {} via [WebSocket, Email, SMS]: {}", userId, title);
    }

    public void sendEmailNotification(String toEmail, String subject, String body) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data("[Pizza Palace] " + subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .data(buildEmailHtml(subject, body))
                                            .charset("UTF-8").build())
                                    .text(Content.builder().data(body).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(request);
            log.debug("Email sent to {}: {}", toEmail, subject);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendSmsNotification(String phoneNumber, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .phoneNumber(phoneNumber)
                    .message("Pizza Palace: " + message)
                    .build();

            snsClient.publish(request);
            log.debug("SMS sent to {}", phoneNumber);
        } catch (Exception e) {
            log.warn("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }

    private String buildEmailHtml(String subject, String body) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: #dc2626; padding: 20px; border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">Pizza Palace</h1>
                </div>
                <div style="background: #ffffff; padding: 24px; border: 1px solid #e5e7eb; border-radius: 0 0 8px 8px;">
                    <h2 style="color: #111827; margin-top: 0;">%s</h2>
                    <p style="color: #4b5563; line-height: 1.6;">%s</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;">
                    <p style="color: #9ca3af; font-size: 12px;">This is an automated message from Pizza Palace. Do not reply to this email.</p>
                </div>
            </body>
            </html>""", subject, body);
    }

    private NotificationDTO mapToDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId()).type(n.getType()).title(n.getTitle())
                .message(n.getMessage()).data(n.getData())
                .isRead(n.getIsRead()).createdAt(n.getCreatedAt())
                .build();
    }
}
