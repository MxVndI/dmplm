package com.diplom.rest.controller;

import com.diplom.persistance.entity.UserTestParticipationEntity;
import com.diplom.domain.service.ABTestService;
import com.diplom.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/ab-tests")
@RequiredArgsConstructor
public class ABTestController {

    private final ABTestService abTestService;
    private final UserService userService;

    @GetMapping
    public String listTests() {
        return "redirect:/admin/config-tests";
    }

    @PostMapping("/create")
    public String createTest(@RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String expiresAt) {
        LocalDateTime expires = null;
        if (expiresAt != null && !expiresAt.isBlank()) {
            try {
                // datetime-local sends "yyyy-MM-ddTHH:mm" (no seconds)
                expires = LocalDateTime.parse(expiresAt,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            } catch (DateTimeParseException e) {
                expires = LocalDateTime.parse(expiresAt); // ISO fallback
            }
        }
        abTestService.createTest(name, description, expires);
        return "redirect:/admin/ab-tests";
    }

    @PostMapping("/{testId}/deactivate")
    public String deactivateTest(@PathVariable String testId) {
        abTestService.deactivateTest(testId);
        return "redirect:/admin/ab-tests";
    }

    @PostMapping("/{testId}/delete")
    public String deleteTest(@PathVariable String testId) {
        abTestService.deleteTest(testId);
        return "redirect:/admin/ab-tests";
    }

    @GetMapping("/{testId}/participants")
    public String listParticipants(@PathVariable String testId, Model model) {
        List<UserTestParticipationEntity> participations = abTestService.findParticipationsByTest(testId);

        // Enrich participations with user details for display
        List<Map<String, Object>> enriched = participations.stream().map(p -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("participation", p);
            userService.findById(p.getUserId()).ifPresent(u -> entry.put("user", u));
            return entry;
        }).toList();

        model.addAttribute("enrichedParticipations", enriched);
        model.addAttribute("test", abTestService.findTestById(testId));
        model.addAttribute("testId", testId);
        model.addAttribute("allUsers", userService.findAll());
        return "admin/ab-test-participants";
    }

    @PostMapping("/{testId}/enroll")
    public String enrollUser(@PathVariable String testId,
                             @RequestParam String userId,
                             @RequestParam(required = false) String variant,
                             Model model) {
        try {
            abTestService.enrollUser(testId, userId, variant);
        } catch (IllegalArgumentException e) {
            model.addAttribute("enrollError", e.getMessage());
            return listParticipants(testId, model);
        }
        return "redirect:/admin/ab-tests/" + testId + "/participants";
    }

    @PostMapping("/participations/{participationId}/remove")
    public String removeParticipation(@PathVariable String participationId,
                                      @RequestParam String testId) {
        abTestService.removeParticipation(participationId);
        return "redirect:/admin/ab-tests/" + testId + "/participants";
    }
}
