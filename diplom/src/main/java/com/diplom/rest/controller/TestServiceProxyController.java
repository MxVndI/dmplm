package com.diplom.rest.controller;

import com.diplom.persistance.repository.UserTestParticipationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Proxies all A/B test management API calls from the browser
 * through the shop container to the test-service container.
 *
 * Browser → /admin/api/proxy/tests/** (same-origin, no CORS)
 *         → http://test-service:8081/api/tests/** (Docker internal network)
 *
 * This avoids all CORS complexity — the browser never speaks directly
 * to port 8081.
 */
@Slf4j
@RestController
@RequestMapping("/admin/api/proxy/tests")
public class TestServiceProxyController {

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    @Autowired
    private UserTestParticipationRepository participationRepository;

    @Value("${app.test-service-url:http://localhost:8081}")
    private String testServiceUrl;

    @Value("${app.test-service-admin-username:admin}")
    private String adminUsername;

    @Value("${app.test-service-admin-password:Admin1234!}")
    private String adminPassword;

    private String base() {
        return testServiceUrl + "/api/tests";
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private HttpHeaders jsonHeadersWithAuth() {
        HttpHeaders h = jsonHeaders();
        String credentials = adminUsername + ":" + adminPassword;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        h.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return h;
    }

    // ── GET /api/tests ────────────────────────────────────────
    @GetMapping
    public ResponseEntity<String> getAll() {
        return forward(HttpMethod.GET, base(), null);
    }

    // ── POST /api/tests ───────────────────────────────────────
    @PostMapping
    public ResponseEntity<String> create(@RequestBody String body) {
        return forwardWithAuth(HttpMethod.POST, base(), body);
    }

    // ── GET /api/tests/{id} ───────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable String id) {
        return forward(HttpMethod.GET, base() + "/" + id, null);
    }

    // ── POST /api/tests/{id}/trigger ──────────────────────────
    @PostMapping("/{id}/trigger")
    public ResponseEntity<String> trigger(@PathVariable String id) {
        return forwardWithAuth(HttpMethod.POST, base() + "/" + id + "/trigger", null);
    }

    // ── PATCH /api/tests/{id}/activate ────────────────────────
    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activate(@PathVariable String id) {
        return forwardWithAuth(HttpMethod.PATCH, base() + "/" + id + "/activate", null);
    }

    // ── PATCH /api/tests/{id}/complete ────────────────────────
    @PatchMapping("/{id}/complete")
    public ResponseEntity<String> complete(@PathVariable String id) {
        ResponseEntity<String> response = forwardWithAuth(HttpMethod.PATCH, base() + "/" + id + "/complete", null);
        if (response.getStatusCode().is2xxSuccessful()) {
            participationRepository.deleteByTestId(id);
            log.info("Cleared shop participations for completed test {}", id);
        }
        return response;
    }

    // ── GET /api/tests/{id}/participants ──────────────────────
    @GetMapping("/{id}/participants")
    public ResponseEntity<String> participants(@PathVariable String id) {
        return forward(HttpMethod.GET, base() + "/" + id + "/participants", null);
    }

    // ── GET /api/tests/{id}/stats ─────────────────────────────
    @GetMapping("/{id}/stats")
    public ResponseEntity<String> stats(@PathVariable String id) {
        return forward(HttpMethod.GET, base() + "/" + id + "/stats", null);
    }

    // ── PUT /api/tests/{id} — update test ─────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        return forwardWithAuth(HttpMethod.PUT, base() + "/" + id, body);
    }

    // ── POST /api/tests/{id}/restart ──────────────────────────
    @PostMapping("/{id}/restart")
    public ResponseEntity<String> restart(@PathVariable String id) {
        participationRepository.deleteByTestId(id);
        return forwardWithAuth(HttpMethod.POST, base() + "/" + id + "/restart", null);
    }

    // ── DELETE /api/tests/{id} ────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        ResponseEntity<String> response = forwardWithAuth(HttpMethod.DELETE, base() + "/" + id, null);
        if (response.getStatusCode().is2xxSuccessful()) {
            participationRepository.deleteByTestId(id);
            log.info("Cleared shop participations for deleted test {}", id);
        }
        return response;
    }

    // ── Internal proxy helpers ────────────────────────────────
    private ResponseEntity<String> forward(HttpMethod method, String url, String body) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(body, jsonHeaders());
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Proxy received error from test-service: {} {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Proxy call to test-service failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"Test-service unavailable: " + e.getMessage() + "\"}");
        }
    }

    private ResponseEntity<String> forwardWithAuth(HttpMethod method, String url, String body) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(body, jsonHeadersWithAuth());
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Proxy received error from test-service: {} {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Proxy call to test-service failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"Test-service unavailable: " + e.getMessage() + "\"}");
        }
    }
}
