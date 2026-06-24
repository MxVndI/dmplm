package com.diplom.notification.rest.controller;

import com.diplom.notification.constant.AppConstants;
import com.diplom.notification.domain.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class TelegramNotificationController {

    private final TelegramService telegramService;

    @PostMapping("/telegram")
    public ResponseEntity<Map<String, String>> sendTelegramNotification(@RequestBody Map<String, Object> payload) {
        try {
            String text = (String) payload.get("text");
            String channel = (String) payload.getOrDefault("channel", AppConstants.DEFAULT_CHANNEL);

            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(AppConstants.ERROR_FIELD, AppConstants.TEXT_REQUIRED));
            }

            if (AppConstants.ADMIN_CHANNEL.equalsIgnoreCase(channel)) {
                telegramService.notifyAdmins(text);
            } else {
                log.warn("Unknown notification channel: {}", channel);
            }

            return ResponseEntity.ok(Map.of(AppConstants.STATUS_FIELD, AppConstants.STATUS_SENT));
        } catch (Exception e) {
            log.error("Error sending Telegram notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(AppConstants.ERROR_FIELD, e.getMessage()));
        }
    }

    @PostMapping("/telegram/admin-alert")
    public ResponseEntity<Map<String, String>> sendAdminAlert(@RequestBody Map<String, String> payload) {
        try {
            String message = payload.get("message");
            if (message == null || message.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(AppConstants.ERROR_FIELD, AppConstants.MESSAGE_REQUIRED));
            }

            telegramService.notifyAdmins(message);
            return ResponseEntity.ok(Map.of(AppConstants.STATUS_FIELD, AppConstants.STATUS_ALERT_SENT));
        } catch (Exception e) {
            log.error("Error sending admin alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(AppConstants.ERROR_FIELD, e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                AppConstants.STATUS_FIELD, AppConstants.STATUS_HEALTHY,
                AppConstants.SERVICE_FIELD, AppConstants.TELEGRAM_NOTIFICATION_SERVICE));
    }
}
