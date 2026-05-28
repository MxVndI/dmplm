package com.diplom.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/notifications")
public class AdminNotificationsController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.test-service-url:http://localhost:8081}")
    private String testServiceUrl;

    @GetMapping
    public String index(Model model) {
        List<Map<String, Object>> activeTests = Collections.emptyList();
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    testServiceUrl + "/api/tests",
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null) {
                activeTests = resp.getBody().stream()
                        .filter(t -> {
                            Object status = t.get("status");
                            return "ACTIVE".equals(status) || "RUNNING".equals(status);
                        })
                        .toList();
            }
        } catch (Exception e) {
            log.warn("Could not fetch tests from test-service: {}", e.getMessage());
        }
        model.addAttribute("activeTests", activeTests);
        return "admin/notifications";
    }
}
