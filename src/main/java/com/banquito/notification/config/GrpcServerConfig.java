package com.banquito.notification.config;

import com.banquito.notification.grpc.NotificationGrpcEndpoint;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    private Server server;

    @Bean
    public Server notificationGrpcServer(
            NotificationGrpcEndpoint endpoint,
            @Value("${grpc.server.port}") int port) throws IOException {
        this.server = ServerBuilder.forPort(port)
                .addService(endpoint)
                .build()
                .start();
        return server;
    }

    @PreDestroy
    public void shutdown() {
        if (server != null) {
            server.shutdown();
        }
    }
}
