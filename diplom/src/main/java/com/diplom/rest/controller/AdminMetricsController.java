package com.diplom.rest.controller;

import com.diplom.persistance.entity.UserEventEntity;
import com.diplom.domain.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/metrics")
@RequiredArgsConstructor
@Slf4j
public class AdminMetricsController {

    private final MetricsService metricsService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.test-service-url:http://localhost:8081}")
    private String testServiceUrl;

    @GetMapping
    public String overview(Model model) {
        List<Map<String, Object>> tests = Collections.emptyList();
        long activeTestsCount = 0L;
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    testServiceUrl + "/api/tests",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            if (resp.getBody() != null) {
                tests = resp.getBody();
                activeTestsCount = tests.stream()
                        .map(t -> t.get("status"))
                        .filter(s -> "RUNNING".equals(s) || "ACTIVE".equals(s))
                        .count();
            }
        } catch (Exception e) {
            log.warn("Could not load tests for metrics page: {}", e.getMessage());
        }

        List<UserEventEntity> recent = metricsService.getRecentEvents(50);
        model.addAttribute("tests", tests);
        model.addAttribute("activeTestsCount", activeTestsCount);
        model.addAttribute("totalTestsCount", tests.size());
        model.addAttribute("recentEvents", recent);
        return "admin/metrics";
    }

    @GetMapping("/test/{testId}")
    public String testDetail(@PathVariable String testId, Model model) {
        Map<String, Object> test = Map.of(
                "id", testId,
                "name", "Тест " + testId,
                "status", "UNKNOWN"
        );
        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    testServiceUrl + "/api/tests/" + testId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            if (resp.getBody() != null) {
                test = resp.getBody();
            }
        } catch (Exception e) {
            log.warn("Could not load test {} for metrics detail: {}", testId, e.getMessage());
        }

        Map<String, Object> summary = metricsService.getTestSummary(testId);
        List<UserEventEntity> events = metricsService.getEventsByTest(testId);
        Object statusObj = test.get("status");
        String status = statusObj == null ? "UNKNOWN" : statusObj.toString();
        model.addAttribute("test", test);
        model.addAttribute("testStatus", status);
        model.addAttribute("testActive", "RUNNING".equals(status) || "ACTIVE".equals(status));
        model.addAttribute("summary", summary);
        model.addAttribute("events", events);
        return "admin/metrics-test";
    }
}
