package com.diplom.rest.controller;

import com.diplom.persistance.entity.UserEntity;
import com.diplom.persistance.entity.UserEventEntity;
import com.diplom.domain.service.MetricsService;
import com.diplom.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;
    private final UserService userService;

    /**
     * Record a user behaviour event from the front-end.
     * userId is always overridden from the authenticated session — never trusted from the body.
     */
    @PostMapping("/event")
    public ResponseEntity<Void> recordEvent(
            @RequestBody UserEventEntity event,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails != null) {
            userService.findByLogin(userDetails.getUsername())
                    .map(UserEntity::getId)
                    .ifPresent(event::setUserId);
        }
        metricsService.recordEvent(event);
        return ResponseEntity.noContent().build();
    }

    /** Get full event list for a test enriched with per-variant summary. */
    @GetMapping("/test/{testId}")
    public ResponseEntity<Map<String, Object>> getTestMetrics(@PathVariable String testId) {
        return ResponseEntity.ok(metricsService.getTestSummary(testId));
    }

    /** Get all events for a user. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMetrics(@PathVariable String userId) {
        return ResponseEntity.ok(metricsService.getEventsByUser(userId));
    }
}
