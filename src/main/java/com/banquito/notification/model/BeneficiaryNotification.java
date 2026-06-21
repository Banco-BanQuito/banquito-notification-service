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

    private BeneficiaryNotification(Builder builder) {
        this.paymentDetailId = builder.paymentDetailId;
        this.emailTo = builder.emailTo;
        this.subject = builder.subject;
        this.messageBody = builder.messageBody;
        this.status = builder.status;
        this.retryCount = builder.retryCount;
        this.nextRetryAt = builder.nextRetryAt;
        this.createdAt = builder.createdAt;
        this.sentAt = builder.sentAt;
        this.errorMessage = builder.errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String paymentDetailId;
        private String emailTo;
        private String subject;
        private String messageBody;
        private String status;
        private int retryCount;
        private Instant nextRetryAt;
        private Instant createdAt;
        private Instant sentAt;
        private String errorMessage;

        public Builder paymentDetailId(String paymentDetailId) {
            this.paymentDetailId = paymentDetailId;
            return this;
        }

        public Builder emailTo(String emailTo) {
            this.emailTo = emailTo;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder messageBody(String messageBody) {
            this.messageBody = messageBody;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder nextRetryAt(Instant nextRetryAt) {
            this.nextRetryAt = nextRetryAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder sentAt(Instant sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public BeneficiaryNotification build() {
            return new BeneficiaryNotification(this);
        }
    }

    public String getId() {
        return id;
    }

    public String getPaymentDetailId() {
        return paymentDetailId;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
