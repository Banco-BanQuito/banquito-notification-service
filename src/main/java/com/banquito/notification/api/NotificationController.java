package com.banquito.notification.api;

import com.banquito.notification.model.NotificationRequest;
import com.banquito.notification.model.NotificationResponse;
import com.banquito.notification.service.NotificationSenderService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/notifications")
public class NotificationController {

    private final NotificationSenderService senderService;

    public NotificationController(NotificationSenderService senderService) {
        this.senderService = senderService;
    }

    @PostMapping("/send")
    public NotificationResponse send(@Valid @RequestBody NotificationRequest request) {
        return senderService.send(request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "notification-service", "version", "2.0");
    }
}
