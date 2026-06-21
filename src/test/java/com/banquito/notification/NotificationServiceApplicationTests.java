package com.banquito.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "grpc.server.port=0",
        "banquito.notification.smtp-enabled=false",
        "banquito.notification.audit-enabled=false"
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
        // Intencional: solo verifica que el contexto de Spring se levante sin errores.
    }
}
