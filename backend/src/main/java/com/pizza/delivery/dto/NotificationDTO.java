package com.pizza.delivery.dto;

import com.pizza.delivery.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationDTO {
    private String id;
    private NotificationType type;
    private String title;
    private String message;
    private String data;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
