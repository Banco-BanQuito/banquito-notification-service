package com.banquito.notification.grpc;

import com.banquito.notification.model.NotificationRequest;
import com.banquito.notification.model.NotificationResponse;
import com.banquito.notification.service.NotificationSenderService;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

@Component
public class NotificationGrpcEndpoint extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final NotificationSenderService senderService;

    public NotificationGrpcEndpoint(NotificationSenderService senderService) {
        this.senderService = senderService;
    }

    @Override
    public void sendNotification(SendNotificationRequest request,
                                 StreamObserver<SendNotificationResponse> responseObserver) {
        NotificationResponse response = senderService.send(new NotificationRequest(
                request.getPaymentDetailId(),
                request.getEmailTo(),
                request.getSubject(),
                request.getBodyTemplate(),
                request.getVariablesMap()
        ));

        responseObserver.onNext(SendNotificationResponse.newBuilder()
                .setNotificationId(response.notificationId())
                .setStatus(response.status())
                .setSentAt(response.sentAt())
                .setErrorMessage(response.errorMessage() == null ? "" : response.errorMessage())
                .build());
        responseObserver.onCompleted();
    }
}
