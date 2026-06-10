package com.banquito.notification.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "beneficiary_notification")
public class BeneficiaryNotification {

    @Id
    private String id;

    @Field("payment_detail_id")
    private String paymentDetailId;

    @Field("email_to")
    private String emailTo;

    private String subject;

    @Field("message_body")
    private String messageBody;

    private String status;

    @Field("retry_count")
    private int retryCount;

    @Field("next_retry_at")
    private Instant nextRetryAt;

    @Field("created_at")
    private Instant createdAt;

    @Field("sent_at")
    private Instant sentAt;

    @Field("error_message")
    private String errorMessage;

    public BeneficiaryNotification(String paymentDetailId, String emailTo, String subject,
                                   String messageBody, String status, int retryCount,
                                   Instant nextRetryAt, Instant createdAt, Instant sentAt,
                                   String errorMessage) {
        this.paymentDetailId = paymentDetailId;
        this.emailTo = emailTo;
        this.subject = subject;
        this.messageBody = messageBody;
        this.status = status;
        this.retryCount = retryCount;
        this.nextRetryAt = nextRetryAt;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.errorMessage = errorMessage;
    }
}
