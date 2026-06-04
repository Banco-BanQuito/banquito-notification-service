package com.banquito.notification.model;

public record NotificationResponse(
        String notificationId,
        String status,
        String sentAt,
        String errorMessage
) {
}
