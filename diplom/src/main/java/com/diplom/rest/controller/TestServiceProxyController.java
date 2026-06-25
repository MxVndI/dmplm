package com.diplom.rest.controller;

import com.diplom.constant.AppConstants;
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

    @Value("${app.test-service-admin-password:}")
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

    private ResponseEntity<String> relay(ResponseEntity<String> response) {
        MediaType contentType = response.getHeaders().getContentType();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.getStatusCode());
        if (contentType != null) {
            builder.contentType(contentType);
        }
        return builder.body(response.getBody());
    }

    @GetMapping
    public ResponseEntity<String> getAll() {
        return forward(HttpMethod.GET, base(), null);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody String body) {
        return forwardWithAuth(HttpMethod.POST, base(), body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable String id) {
        return forward(HttpMethod.GET, base() + "/" + id, null);
    }

    @PostMapping("/{id}/trigger")
    public ResponseEntity<String> trigger(@PathVariable String id) {
        return forwardWithAuth(HttpMethod.POST, base() + "/" + id + "/trigger", null);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activate(@PathVariable String id) {
        return forwardWithAuth(HttpMethod.PATCH, base() + "/" + id + "/activate", null);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<String> complete(@PathVariable String id) {
        ResponseEntity<String> response = forwardWithAuth(HttpMethod.PATCH, base() + "/" + id + "/complete", null);
        if (response.getStatusCode().is2xxSuccessful()) {
            participationRepository.deleteByTestId(id);
            log.info("Cleared shop participations for completed test {}", id);
        }
        return response;
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<String> participants(@PathVariable String id) {
        return forward(HttpMethod.GET, base() + "/" + id + "/participants", null);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<String> stats(@PathVariable String id) {
        return forward(HttpMethod.GET, base() + "/" + id + "/stats", null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody String body) {
        return forwardWithAuth(HttpMethod.PUT, base() + "/" + id, body);
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<String> restart(@PathVariable String id) {
        participationRepository.deleteByTestId(id);
        return forwardWithAuth(HttpMethod.POST, base() + "/" + id + "/restart", null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        ResponseEntity<String> response = forwardWithAuth(HttpMethod.DELETE, base() + "/" + id, null);
        if (response.getStatusCode().is2xxSuccessful()) {
            participationRepository.deleteByTestId(id);
            log.info("Cleared shop participations for deleted test {}", id);
        }
        return response;
    }

    private ResponseEntity<String> forward(HttpMethod method, String url, String body) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(body, jsonHeaders());
            return relay(restTemplate.exchange(url, method, entity, String.class));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Proxy received error from test-service: {} {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Proxy call to test-service failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"" + AppConstants.TEST_SERVICE_UNAVAILABLE + e.getMessage() + "\"}");
        }
    }

    private ResponseEntity<String> forwardWithAuth(HttpMethod method, String url, String body) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(body, jsonHeadersWithAuth());
            return relay(restTemplate.exchange(url, method, entity, String.class));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Proxy received error from test-service: {} {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Proxy call to test-service failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"" + AppConstants.TEST_SERVICE_UNAVAILABLE + e.getMessage() + "\"}");
        }
    }
}
