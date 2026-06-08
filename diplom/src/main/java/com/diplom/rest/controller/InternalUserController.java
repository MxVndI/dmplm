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

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userService.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "login", u.getLogin() != null ? u.getLogin() : "",
                        "email", u.getEmail() != null ? u.getEmail() : "",
                        "firstName", u.getFirstName() != null ? u.getFirstName() : "",
                        "lastName", u.getLastName() != null ? u.getLastName() : "",
                        "telegramChatId", u.getTelegramChatId() != null ? u.getTelegramChatId() : ""
                ))
                .toList();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String userId) {
        Optional<UserEntity> userOpt = userService.findById(userId);
        return userOpt.map(u -> ResponseEntity.ok(Map.<String, Object>of(
                "id", u.getId(),
                "login", u.getLogin() != null ? u.getLogin() : "",
                "email", u.getEmail() != null ? u.getEmail() : "",
                "firstName", u.getFirstName() != null ? u.getFirstName() : "",
                "lastName", u.getLastName() != null ? u.getLastName() : "",
                "telegramChatId", u.getTelegramChatId() != null ? u.getTelegramChatId() : ""
        ))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/by-phone/{phone}")
    public ResponseEntity<Map<String, Object>> findByPhone(@PathVariable String phone) {
        Optional<UserEntity> userOpt = userRepository.findByPhone(phone);
        return userOpt.map(u -> ResponseEntity.ok(Map.<String, Object>of(
                "id", u.getId(),
                "login", u.getLogin() != null ? u.getLogin() : "",
                "firstName", u.getFirstName() != null ? u.getFirstName() : "",
                "telegramChatId", u.getTelegramChatId() != null ? u.getTelegramChatId() : ""
        ))).orElseGet(() -> ResponseEntity.notFound().build());
    }

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
