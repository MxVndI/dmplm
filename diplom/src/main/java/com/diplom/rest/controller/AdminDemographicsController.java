package com.diplom.rest.controller;

import com.diplom.persistance.entity.UserEntity;
import com.diplom.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/admin/demographics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDemographicsController {

    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.demographic-service-url:http://localhost:8084}")
    private String demographicServiceUrl;

    @GetMapping
    public String page(Model model) {
        List<UserEntity> users = userService.findAll();
        model.addAttribute("users", users);

        Map<String, Map<String, Object>> demoMap = new LinkedHashMap<>();
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    demographicServiceUrl + "/api/demographics",
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null) {
                for (Map<String, Object> d : resp.getBody()) {
                    Object uid = d.get("userId");
                    if (uid != null) demoMap.put(uid.toString(), d);
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch demographics: {}", e.getMessage());
            model.addAttribute("demoError", "Сервис демографии недоступен: " + e.getMessage());
        }
        model.addAttribute("demoMap", demoMap);
        return "admin/demographics";
    }

    @PostMapping("/sync")
    public String sync(@RequestParam String userId) {
        userService.findById(userId).ifPresent(user -> {
            try {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("userId", user.getId());
                // Basic demographic data from user profile
                if (user.getAge() != null)      body.put("age", user.getAge());
                if (user.getCountry() != null && !user.getCountry().isBlank())   body.put("country", user.getCountry());
                if (user.getLanguage() != null && !user.getLanguage().isBlank()) body.put("language", user.getLanguage());
                if (user.getGender() != null)    body.put("gender", user.getGender().name());

                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                org.springframework.http.HttpEntity<Map<String, Object>> req = new org.springframework.http.HttpEntity<>(body, headers);
                restTemplate.postForEntity(demographicServiceUrl + "/api/demographics", req, String.class);
                log.info("Manually synced demographics for userId={}", userId);
            } catch (Exception e) {
                log.warn("Manual sync failed for userId={}: {}", userId, e.getMessage());
            }
        });
        return "redirect:/admin/demographics";
    }

    @PostMapping("/sync-all")
    public String syncAll() {
        userService.findAll().forEach(user -> {
            try {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("userId", user.getId());
                if (user.getAge() != null)      body.put("age", user.getAge());
                if (user.getCountry() != null && !user.getCountry().isBlank())   body.put("country", user.getCountry());
                if (user.getLanguage() != null && !user.getLanguage().isBlank()) body.put("language", user.getLanguage());
                if (user.getGender() != null)    body.put("gender", user.getGender().name());

                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                org.springframework.http.HttpEntity<Map<String, Object>> req = new org.springframework.http.HttpEntity<>(body, headers);
                restTemplate.postForEntity(demographicServiceUrl + "/api/demographics", req, String.class);
            } catch (Exception e) {
                log.warn("Bulk sync failed for userId={}: {}", user.getId(), e.getMessage());
            }
        });
        return "redirect:/admin/demographics";
    }
}
