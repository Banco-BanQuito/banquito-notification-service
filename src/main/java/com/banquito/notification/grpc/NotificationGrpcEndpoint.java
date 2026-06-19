package com.banquito.notification.grpc;

import com.banquito.notification.model.NotificationRequest;
import com.banquito.notification.model.NotificationResponse;
import com.banquito.notification.service.NotificationSenderService;
import com.banquito.payswitch.notification.NotificationResponse.Builder;
import com.banquito.payswitch.notification.NotificationServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class NotificationGrpcEndpoint extends NotificationServiceGrpc.NotificationServiceImplBase {

    private final NotificationSenderService senderService;

    public NotificationGrpcEndpoint(NotificationSenderService senderService) {
        this.senderService = senderService;
    }

    @Override
    public void sendNotification(com.banquito.payswitch.notification.NotificationRequest request,
                                 StreamObserver<com.banquito.payswitch.notification.NotificationResponse> responseObserver) {
        NotificationResponse response = senderService.send(new NotificationRequest(
                String.valueOf(request.getPaymentDetailId()),
                request.getEmailTo(),
                request.getSubject(),
                request.getBodyTemplate(),
                request.getVariablesMap()
        ));

        Builder builder = com.banquito.payswitch.notification.NotificationResponse.newBuilder()
                .setNotificationId(response.notificationId())
                .setStatus(response.status());
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
