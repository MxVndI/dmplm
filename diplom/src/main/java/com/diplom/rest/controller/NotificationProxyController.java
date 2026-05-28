package com.diplom.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxies notification campaign API calls from the browser to notification-service.
 *
 * Browser → /admin/api/proxy/campaigns/**
 *         → http://notification-service:8083/api/campaigns/**
 */
@Slf4j
@RestController
@RequestMapping("/admin/api/proxy/campaigns")
public class NotificationProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.notification-service-url:http://localhost:8083}")
    private String notificationServiceUrl;

    private String base() {
        return notificationServiceUrl + "/api/campaigns";
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @GetMapping
    public ResponseEntity<String> getAll() {
        return forward(HttpMethod.GET, base(), null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable String id) {
        return forward(HttpMethod.GET, base() + "/" + id, null);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody String body) {
        return forward(HttpMethod.POST, base(), body);
    }

    @PostMapping("/ab-pair")
    public ResponseEntity<String> createAbPair(@RequestBody String body) {
        return forward(HttpMethod.POST, base() + "/ab-pair", body);
    }

    @GetMapping("/ab-stats/{testId}")
    public ResponseEntity<String> abStats(@PathVariable String testId) {
        return forward(HttpMethod.GET, base() + "/ab-stats/" + testId, null);
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<String> send(@PathVariable String id) {
        return forward(HttpMethod.POST, base() + "/" + id + "/send", null);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<String> stats(@PathVariable String id) {
        return forward(HttpMethod.GET, base() + "/" + id + "/stats", null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        return forward(HttpMethod.DELETE, base() + "/" + id, null);
    }

    private ResponseEntity<String> forward(HttpMethod method, String url, String body) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(body, jsonHeaders());
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Notification proxy error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
