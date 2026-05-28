package com.diplom.notification.rest.controller;

import com.diplom.notification.rest.dto.CreateAbCampaignDto;
import com.diplom.notification.rest.dto.CreateCampaignDto;
import com.diplom.notification.persistance.entity.NotificationCampaignEntity;
import com.diplom.notification.domain.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public List<NotificationCampaignEntity> listAll() {
        return campaignService.findAll();
    }

    @GetMapping("/{id}")
    public NotificationCampaignEntity getById(@PathVariable String id) {
        return campaignService.findById(id);
    }

    @PostMapping
    public ResponseEntity<NotificationCampaignEntity> create(@Valid @RequestBody CreateCampaignDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(dto));
    }

    @PostMapping("/ab-pair")
    public ResponseEntity<List<NotificationCampaignEntity>> createAbPair(@RequestBody CreateAbCampaignDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.createAbPair(dto));
    }

    @GetMapping("/ab-stats/{testId}")
    public List<Map<String, Object>> abStats(@PathVariable String testId) {
        return campaignService.getAbComparisonStats(testId);
    }

    @PostMapping("/{id}/send")
    public NotificationCampaignEntity send(@PathVariable String id) {
        return campaignService.send(id);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Long> stats(@PathVariable String id) {
        return campaignService.getDeliveryStats(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> notFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
