package com.diplom.notification.domain.service;

import com.diplom.notification.persistance.entity.DeliveryStatus;
import com.diplom.notification.persistance.repository.NotificationDeliveryRepository;
import com.diplom.notification.utils.ShopUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationDeliveryRepository deliveryRepository;
    private final ShopUserClient shopUserClient;

    @Value("${spring.mail.username:noreply@diplom-shop.local}")
    private String senderEmail;

    @Value("${app.notification.email-enabled:true}")
    private boolean emailEnabled;

    public void sendEmail(String recipientEmail, String subject, String body, String deliveryId) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled. Skipping email to {}", recipientEmail);
            updateDeliveryStatus(deliveryId, DeliveryStatus.SKIPPED);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", recipientEmail, subject);
            updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERED);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", recipientEmail, e.getMessage());
            updateDeliveryStatus(deliveryId, DeliveryStatus.FAILED);
        }
    }

    public void sendHtmlEmail(String recipientEmail, String subject, String htmlBody, String deliveryId) {
        if (!emailEnabled) {
            log.debug("Email sending is disabled. Skipping HTML email to {}", recipientEmail);
            updateDeliveryStatus(deliveryId, DeliveryStatus.SKIPPED);
            return;
        }

        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("HTML email sent to {} — subject: {}", recipientEmail, subject);
            updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERED);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", recipientEmail, e.getMessage());
            updateDeliveryStatus(deliveryId, DeliveryStatus.FAILED);
        }
    }

    public void sendCampaignEmail(String userId, String deliveryId, String subject, String body) {
        try {
            ShopUserClient.UserInfo user = shopUserClient.getUser(userId);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Cannot send email to user {} — no email address", userId);
                return;
            }

            sendEmail(user.getEmail(), subject, body, deliveryId);
        } catch (Exception e) {
            log.error("Error retrieving user {} for email: {}", userId, e.getMessage());
            updateDeliveryStatus(deliveryId, DeliveryStatus.FAILED);
        }
    }

    private void updateDeliveryStatus(String deliveryId, DeliveryStatus status) {
        try {
            deliveryRepository.findById(deliveryId).ifPresent(delivery -> {
                delivery.setStatus(status);
                delivery.setSentAt(LocalDateTime.now());
                deliveryRepository.save(delivery);
            });
        } catch (Exception e) {
            log.warn("Could not update delivery status for {}: {}", deliveryId, e.getMessage());
        }
    }
}
