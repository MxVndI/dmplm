package com.diplom.notification.rest.controller;

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
            String channel = (String) payload.getOrDefault("channel", "default");

            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "text is required"));
            }

            if ("admin".equalsIgnoreCase(channel)) {
                telegramService.notifyAdmins(text);
            } else {
                log.warn("Unknown notification channel: {}", channel);
            }

            return ResponseEntity.ok(Map.of("status", "sent"));
        } catch (Exception e) {
            log.error("Error sending Telegram notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/telegram/admin-alert")
    public ResponseEntity<Map<String, String>> sendAdminAlert(@RequestBody Map<String, String> payload) {
        try {
            String message = payload.get("message");
            if (message == null || message.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "message is required"));
            }

            telegramService.notifyAdmins(message);
            return ResponseEntity.ok(Map.of("status", "alert_sent"));
        } catch (Exception e) {
            log.error("Error sending admin alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "healthy", "service", "telegram-notification"));
    }
}
