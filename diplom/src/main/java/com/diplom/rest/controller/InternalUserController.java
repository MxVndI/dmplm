package com.diplom.rest.controller;

import com.diplom.persistance.entity.UserEntity;
import com.diplom.persistance.repository.UserRepository;
import com.diplom.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Internal API — accessed only by other services inside the Docker network.
 * Not exposed in the admin UI. Provides user data for notifications, etc.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Returns a lightweight projection of all users,
     * including email and telegramChatId for notification targeting.
     */
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userService.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id",               u.getId(),
                        "login",            u.getLogin() != null ? u.getLogin() : "",
                        "email",            u.getEmail() != null ? u.getEmail() : "",
                        "firstName",        u.getFirstName() != null ? u.getFirstName() : "",
                        "lastName",         u.getLastName() != null ? u.getLastName() : "",
                        "telegramChatId",   u.getTelegramChatId() != null ? u.getTelegramChatId() : ""
                ))
                .toList();
    }

    /**
     * Get a single user by ID for notification dispatch.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String userId) {
        Optional<UserEntity> userOpt = userService.findById(userId);
        return userOpt.map(u -> ResponseEntity.ok(Map.<String, Object>of(
                "id",               u.getId(),
                "login",            u.getLogin() != null ? u.getLogin() : "",
                "email",            u.getEmail() != null ? u.getEmail() : "",
                "firstName",        u.getFirstName() != null ? u.getFirstName() : "",
                "lastName",         u.getLastName() != null ? u.getLastName() : "",
                "telegramChatId",   u.getTelegramChatId() != null ? u.getTelegramChatId() : ""
        ))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Look up a user by phone number — used by Telegram bot to link accounts.
     * The phone must already be stored and unique in the users collection.
     */
    @GetMapping("/users/by-phone/{phone}")
    public ResponseEntity<Map<String, Object>> findByPhone(@PathVariable String phone) {
        Optional<UserEntity> userOpt = userRepository.findByPhone(phone);
        return userOpt.map(u -> ResponseEntity.ok(Map.<String, Object>of(
                "id",            u.getId(),
                "login",         u.getLogin() != null ? u.getLogin() : "",
                "firstName",     u.getFirstName() != null ? u.getFirstName() : "",
                "telegramChatId",u.getTelegramChatId() != null ? u.getTelegramChatId() : ""
        ))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Link a Telegram chat ID to a user account — called after phone verification.
     */
    @PatchMapping("/users/{userId}/telegram/{chatId}")
    public ResponseEntity<Void> linkTelegram(@PathVariable String userId,
                                              @PathVariable String chatId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setTelegramChatId(chatId);
            userRepository.save(u);
        });
        return ResponseEntity.noContent().build();
    }
}
