package com.banquito.notification.service;

import com.banquito.notification.model.NotificationRequest;
import com.banquito.notification.model.NotificationResponse;
import com.banquito.notification.model.BeneficiaryNotification;
import com.banquito.notification.repository.BeneficiaryNotificationRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationSenderService {

    private static final String STATUS_SENT = "ENVIADO";
    private static final String STATUS_SIMULATED = "SIMULADO";

    private final JavaMailSender mailSender;
    private final BeneficiaryNotificationRepository auditRepository;
    private final String from;
    private final boolean smtpEnabled;
    private final boolean auditEnabled;

    public NotificationSenderService(
            JavaMailSender mailSender,
            BeneficiaryNotificationRepository auditRepository,
            @Value("${banquito.notification.from}") String from,
            @Value("${banquito.notification.smtp-enabled}") boolean smtpEnabled,
            @Value("${banquito.notification.audit-enabled}") boolean auditEnabled) {
        this.mailSender = mailSender;
        this.auditRepository = auditRepository;
        this.from = from;
        this.smtpEnabled = smtpEnabled;
        this.auditEnabled = auditEnabled;
    }

    public NotificationResponse send(NotificationRequest request) {
        if (alreadySent(request.paymentDetailId())) {
            return new NotificationResponse("", STATUS_SENT, Instant.now().toString(), null);
        }

        String notificationId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        if (!smtpEnabled) {
            NotificationResponse response = new NotificationResponse(notificationId, STATUS_SIMULATED, now.toString(), null);
            audit(request, response, now);
            return response;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(request.emailTo());
            message.setSubject(request.subject());
            message.setText(renderBody(request));
            mailSender.send(message);
            NotificationResponse response = new NotificationResponse(notificationId, STATUS_SENT, now.toString(), null);
            audit(request, response, now);
            return response;
        } catch (Exception ex) {
            NotificationResponse response = new NotificationResponse(notificationId, "ERROR", now.toString(), ex.getMessage());
            audit(request, response, now);
            return response;
        }
    }

    private boolean alreadySent(String paymentDetailId) {
        if (!auditEnabled || paymentDetailId == null || paymentDetailId.isBlank()) {
            return false;
        }
        try {
            return auditRepository.findFirstByPaymentDetailIdAndStatus(paymentDetailId, STATUS_SENT).isPresent();
        } catch (Exception ignored) {
            return false;
        }
    }

    private void audit(NotificationRequest request, NotificationResponse response, Instant now) {
        if (!auditEnabled) {
            return;
        }
        boolean sent = STATUS_SENT.equals(response.status()) || STATUS_SIMULATED.equals(response.status());
        try {
            auditRepository.save(new BeneficiaryNotification(
                    request.paymentDetailId(),
                    request.emailTo(),
                    request.subject(),
                    renderBody(request),
                    sent ? STATUS_SENT : response.status(),
                    sent ? 0 : 1,
                    sent ? null : now.plusSeconds(300),
                    now,
                    sent ? now : null,
                    response.errorMessage()
            ));
        } catch (Exception ignored) {
            // El correo no debe fallar por un problema temporal de auditoria Mongo.
        }
    }

    private String renderBody(NotificationRequest request) {
        String amount = request.variables().getOrDefault("amount", "");
        String companyName = request.variables().getOrDefault("companyName", "");
        String concept = request.variables().getOrDefault("concept", "");
        String date = request.variables().getOrDefault("date", "");

        return """
                Estimado beneficiario,

                Ha recibido un pago en BanQuito.

                Empresa emisora: %s
                Monto: %s
                Concepto: %s
                Fecha: %s

                Este mensaje fue generado automaticamente por BanQuito.
                """.formatted(companyName, amount, concept, date);
    }
}
