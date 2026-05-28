package com.diplom.testservice.rest.controller;

import com.diplom.testservice.rest.dto.ABResolutionDto;
import com.diplom.testservice.rest.dto.CreateABConfigDto;
import com.diplom.testservice.rest.dto.CreateABRuleDto;
import com.diplom.testservice.persistance.entity.ABAssignmentEntity;
import com.diplom.testservice.persistance.entity.ABConfigEntity;
import com.diplom.testservice.persistance.entity.ABRuleEntity;
import com.diplom.testservice.domain.service.ABRuleService;
import com.diplom.testservice.mapper.ABRuleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ab")
@RequiredArgsConstructor
public class ABRuleController {

    private final ABRuleService abRuleService;
    private final ABRuleMapper abRuleMapper;

    @GetMapping("/resolve")
    public ResponseEntity<ABResolutionDto> resolve(
            @RequestParam String userId,
            @RequestParam String path) {
        Optional<ABResolutionDto> result = abRuleService.resolve(userId, path);
        return result.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/rules")
    public List<ABRuleEntity> listRules() {
        return abRuleService.getAllRules();
    }

    @PostMapping("/rules")
    public ResponseEntity<ABRuleEntity> createRule(@Valid @RequestBody CreateABRuleDto dto) {
        ABRuleEntity rule = abRuleMapper.toRuleEntity(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(abRuleService.saveRule(rule));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        abRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/configs")
    public List<ABConfigEntity> listConfigs() {
        return abRuleService.getAllConfigs();
    }

    @PostMapping("/configs")
    public ResponseEntity<ABConfigEntity> createConfig(@Valid @RequestBody CreateABConfigDto dto) {
        ABConfigEntity config = abRuleMapper.toConfigEntity(dto);
        if (dto.getId() == null || dto.getId().isBlank()) {
            config.setId(null);
        }
        config.setCreatedAt(Instant.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(abRuleService.saveConfig(config));
    }

    @DeleteMapping("/configs/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String id) {
        abRuleService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignments/{userId}")
    public List<ABAssignmentEntity> getAssignments(@PathVariable String userId) {
        return abRuleService.getAssignmentsByUser(userId);
    }

    @DeleteMapping("/assignments/{userId}/{abTestId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable String userId,
                                                  @PathVariable String abTestId) {
        abRuleService.deleteAssignment(userId, abTestId);
        return ResponseEntity.noContent().build();
    }
}
