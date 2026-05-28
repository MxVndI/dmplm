package com.diplom.testservice.rest.controller;

import com.diplom.testservice.rest.dto.CreateTestDto;
import com.diplom.testservice.persistance.entity.TestConfigEntity;
import com.diplom.testservice.persistance.entity.TestParticipantEntity;
import com.diplom.testservice.domain.service.TestConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestConfigService testConfigService;

    @PostMapping
    public ResponseEntity<TestConfigEntity> create(@Valid @RequestBody CreateTestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testConfigService.create(dto));
    }

    @GetMapping
    public List<TestConfigEntity> listAll() {
        return testConfigService.findAll();
    }

    @GetMapping("/{id}")
    public TestConfigEntity getById(@PathVariable String id) {
        return testConfigService.findById(id);
    }

    @PostMapping("/{id}/trigger")
    public TestConfigEntity triggerSelection(@PathVariable String id) {
        return testConfigService.triggerSelection(id);
    }

    @PatchMapping("/{id}/activate")
    public TestConfigEntity activate(@PathVariable String id) {
        return testConfigService.activate(id);
    }

    @PatchMapping("/{id}/complete")
    public TestConfigEntity complete(@PathVariable String id) {
        return testConfigService.complete(id);
    }

    @PutMapping("/{id}")
    public TestConfigEntity update(@PathVariable String id, @Valid @RequestBody CreateTestDto dto) {
        return testConfigService.update(id, dto);
    }

    @PostMapping("/{id}/restart")
    public TestConfigEntity restart(@PathVariable String id) {
        return testConfigService.restart(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        testConfigService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/participants")
    public List<TestParticipantEntity> getParticipants(@PathVariable String id) {
        return testConfigService.getParticipants(id);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Long> getStats(@PathVariable String id) {
        return testConfigService.getStats(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBadState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
