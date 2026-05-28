package com.diplom.testservice.rest.client;

import com.diplom.testservice.persistance.entity.TestArchiveEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${app.notification-service-url:http://notification-service:8083}")
    private String notificationServiceUrl;

    public void notifyTestExpired(TestArchiveEntity archive) {
        try {
            String message = buildTestExpirationMessage(archive);
            sendTelegramNotification(message);
        } catch (Exception e) {
            log.warn("Failed to send test expiration notification: {}", e.getMessage());
        }
    }

    private void sendTelegramNotification(String text) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", text);
            payload.put("channel", "admin");
            payload.put("parse_mode", "Markdown");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            String url = notificationServiceUrl + "/api/notifications/telegram";

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Test expiration notification sent successfully");
            } else {
                log.warn("Notification service returned status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Error sending Telegram notification: {}", e.getMessage());
        }
    }

    private String buildTestExpirationMessage(TestArchiveEntity archive) {
        String LF = "\n";
        return "✅ *A/B Test Completed*" + LF +
                LF +
                "*Name:* " + archive.getName() + LF +
                "*Duration:* " + archive.getDurationMinutes() + " minutes" + LF +
                LF +
                "*Variant A:*" + LF +
                "  Participants: " + archive.getVariantACount() + LF +
                "  Conversions: " + archive.getVariantAConversions() + " (" + String.format("%.1f%%", archive.getVariantAConversionRate() * 100) + ")" + LF +
                "  Avg Value: $" + String.format("%.2f", archive.getVariantAAvgValuePerUser()) + LF +
                LF +
                "*Variant B:*" + LF +
                "  Participants: " + archive.getVariantBCount() + LF +
                "  Conversions: " + archive.getVariantBConversions() + " (" + String.format("%.1f%%", archive.getVariantBConversionRate() * 100) + ")" + LF +
                "  Avg Value: $" + String.format("%.2f", archive.getVariantBAvgValuePerUser()) + LF +
                LF +
                "*Overall:*" + LF +
                "  Total Participants: " + archive.getTotalParticipants() + LF +
                "  Total Conversions: " + archive.getTotalConversions() + LF +
                "  Conversion Rate: " + String.format("%.1f%%", archive.getOverallConversionRate() * 100);
    }
}
