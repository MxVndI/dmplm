package com.diplom.rest.controller;

import com.diplom.persistance.entity.TestTemplateEntity;
import com.diplom.domain.service.TestTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/api/templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTemplateController {

    private final TestTemplateService templateService;

    @GetMapping
    public List<TestTemplateEntity> listAll() {
        return templateService.findAll();
    }

    @GetMapping("/by-test/{testId}")
    public List<TestTemplateEntity> listByTest(@PathVariable String testId) {
        return templateService.findByTestId(testId);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<TestTemplateEntity> upload(
            @RequestParam String testId,
            @RequestParam String variant,
            @RequestParam(defaultValue = "/") String pagePattern,
            @RequestParam(defaultValue = "Страница") String pageName,
            @RequestParam String name,
            @RequestParam MultipartFile file) {

        String original = file.getOriginalFilename();
        String normalizedVariant = variant == null ? "" : variant.trim().toUpperCase();
        if (original == null || !original.toLowerCase().endsWith(".html")) {
            return ResponseEntity.badRequest().build();
        }
        if (!Set.of("A", "B", "C", "D").contains(normalizedVariant)) {
            return ResponseEntity.badRequest().build();
        }
        TestTemplateEntity result = templateService.upload(
                testId, normalizedVariant, pagePattern, pageName, name, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/apply-preset")
    public ResponseEntity<TestTemplateEntity> applyPreset(
            @RequestBody Map<String, String> body) {
        String testId      = body.get("testId");
        String variant     = body.get("variant");
        String pagePattern = body.getOrDefault("pagePattern", "/");
        String pageName    = body.getOrDefault("pageName", "Страница");
        String name        = body.get("name");
        String html        = body.get("html");

        if (testId == null || variant == null || name == null || html == null) {
            return ResponseEntity.badRequest().build();
        }
        String normalizedVariant = variant.trim().toUpperCase();
        if (!Set.of("A", "B", "C", "D").contains(normalizedVariant)) {
            return ResponseEntity.badRequest().build();
        }
        TestTemplateEntity result = templateService.uploadPreset(
                testId, normalizedVariant, pagePattern, pageName, name, html);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<String> preview(@PathVariable String id) {
        return templateService.findAll().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .map(t -> {
                    String html = templateService.getHtmlContent(t);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
                            .body(html);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
