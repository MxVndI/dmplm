package com.diplom.notification.domain.service;

import com.diplom.notification.persistance.entity.DeliveryStatus;
import com.diplom.notification.persistance.repository.NotificationDeliveryRepository;
import com.diplom.notification.utils.ShopUserClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final NotificationDeliveryRepository deliveryRepository;
    private final ShopUserClient shopUserClient;
    private final RestTemplate restTemplate;

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.admin-chat-ids:}")
    private String adminChatIds;

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";

    private boolean isTelegramEnabled() {
        return botToken != null && !botToken.isBlank();
    }

    public void sendTelegramMessage(String userId, String deliveryId, String text) {
        if (!isTelegramEnabled()) {
            log.debug("Telegram sending skipped — bot token not configured");
            updateDeliveryStatus(deliveryId, DeliveryStatus.SKIPPED);
            return;
        }

        try {
            ShopUserClient.UserInfo user = shopUserClient.getUser(userId);
            if (user == null || user.getTelegramChatId() == null || user.getTelegramChatId().isBlank()) {
                log.warn("Cannot send Telegram message to user {} — no chat ID", userId);
                updateDeliveryStatus(deliveryId, DeliveryStatus.FAILED);
                return;
            }

            sendMessage(user.getTelegramChatId(), text, deliveryId);
        } catch (Exception e) {
            log.error("Error retrieving user {} for Telegram: {}", userId, e.getMessage());
            updateDeliveryStatus(deliveryId, DeliveryStatus.FAILED);
        }
    }

    public void sendMessage(String chatId, String text, String deliveryId) {
        try {
            String url = TELEGRAM_API_BASE + botToken + "/sendMessage";

            URI uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("chat_id", chatId)
                    .queryParam("text", text)
                    .queryParam("parse_mode", "HTML")
                    .build()
                    .toUri();

            String response = restTemplate.getForObject(uri, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);

            if (json.get("ok").asBoolean()) {
                log.info("Telegram message sent to chat {}", chatId);
                updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERED);
            } else {
                String error = json.get("description").asText();
                log.error("Telegram API error: {}", error);
                updateDeliveryStatus(deliveryId, DeliveryStatus.FAILED);
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram message to {}: {}", chatId, e.getMessage());
            updateDeliveryStatus(deliveryId, DeliveryStatus.FAILED);
        }
    }

    public void notifyAdmins(String message) {
        if (!isTelegramEnabled()) {
            log.debug("Telegram admin notification skipped — bot token not configured");
            return;
        }

        if (adminChatIds == null || adminChatIds.isBlank()) {
            log.warn("No admin Telegram chat IDs configured");
            return;
        }

        for (String chatId : adminChatIds.split(",")) {
            try {
                sendMessage(chatId.trim(), "🔔 АДМИН АЛЕРТ\n\n" + message, "admin-alert");
            } catch (Exception e) {
                log.warn("Failed to notify admin {}: {}", chatId, e.getMessage());
            }
        }
    }

    public void notifyTestCompletion(String testName, int variantACount, int variantBCount, double conversionA, double conversionB) {
        String message = "✅ ТЕСТ ЗАВЕРШЁН\n\n" +
                "<b>" + testName + "</b>\n\n" +
                "Вариант A: " + variantACount + " участников | CR: " + String.format("%.2f%%", conversionA * 100) + "\n" +
                "Вариант B: " + variantBCount + " участников | CR: " + String.format("%.2f%%", conversionB * 100) + "\n";
        notifyAdmins(message);
    }

    public void notifyTestStarted(String testName) {
        String message = "🚀 НОВЫЙ ТЕСТ\n\n<b>" + testName + "</b>\n\nТест запущен и собирает данные";
        notifyAdmins(message);
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
