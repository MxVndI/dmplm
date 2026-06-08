package com.diplom.rest.controller;

import com.diplom.domain.service.UserService;
import com.diplom.persistance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public String listUsers(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            model.addAttribute("users", userService.findAll().stream()
                    .filter(u -> u.getLogin().toLowerCase().contains(q)
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                            || (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q))
                            || (u.getLastName() != null && u.getLastName().toLowerCase().contains(q)))
                    .toList());
        } else {
            model.addAttribute("users", userService.findAll());
        }
        model.addAttribute("search", search);
        return "admin/users";
    }

    @PostMapping("/{id}/block")
    public String blockUser(@PathVariable String id) {
        userService.setBlocked(id, true);
        return "redirect:/admin/users?blocked=true";
    }

    @PostMapping("/{id}/unblock")
    public String unblockUser(@PathVariable String id) {
        userService.setBlocked(id, false);
        return "redirect:/admin/users?unblocked=true";
    }

    @PostMapping("/sync-profiles")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncProfiles() {
        var users = userRepository.findAll();
        users.forEach(userService::publishProfile);
        return ResponseEntity.ok(Map.of(
                "published", users.size(),
                "message", "Профили " + users.size() + " пользователей отправлены в Kafka"
        ));
    }
}
