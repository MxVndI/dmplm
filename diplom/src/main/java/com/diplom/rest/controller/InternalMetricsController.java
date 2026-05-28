package com.diplom.rest.controller;

import com.diplom.domain.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal metrics API — accessible without authentication by other services
 * inside the Docker network (secured by SecurityConfig: /internal/** is permitted).
 * Used by the Telegram admin bot to fetch test metrics summaries.
 */
@RestController
@RequestMapping("/internal/metrics")
@RequiredArgsConstructor
public class InternalMetricsController {

    private final MetricsService metricsService;

    /** Aggregated per-variant summary for a given A/B test. */
    @GetMapping("/test/{testId}")
    public Map<String, Object> getTestSummary(@PathVariable String testId) {
        return metricsService.getTestSummary(testId);
    }
}
