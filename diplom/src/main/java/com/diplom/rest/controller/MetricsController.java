package com.diplom.rest.controller;

import com.diplom.domain.service.MetricsService;
import com.diplom.domain.service.UserService;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.persistance.entity.UserEventEntity;
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

    @GetMapping("/test/{testId}")
    public ResponseEntity<Map<String, Object>> getTestMetrics(@PathVariable String testId) {
        return ResponseEntity.ok(metricsService.getTestSummary(testId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMetrics(@PathVariable String userId) {
        return ResponseEntity.ok(metricsService.getEventsByUser(userId));
    }
}
