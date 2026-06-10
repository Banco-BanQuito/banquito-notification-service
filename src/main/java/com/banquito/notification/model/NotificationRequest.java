package com.banquito.notification.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record NotificationRequest(
        @NotBlank String paymentDetailId,
        @Email @NotBlank String emailTo,
        @NotBlank String subject,
        @NotBlank String bodyTemplate,
        @NotNull Map<String, String> variables
) {
}
