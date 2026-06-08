package com.diplom.rest.controller;

import com.diplom.domain.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/metrics")
@RequiredArgsConstructor
public class InternalMetricsController {

    private final MetricsService metricsService;

    @GetMapping("/test/{testId}")
    public Map<String, Object> getTestSummary(@PathVariable String testId) {
        return metricsService.getTestSummary(testId);
    }
}
