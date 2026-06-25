package com.diplom.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/admin/api/proxy/demographics")
public class DemographicProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.demographic-service-url:http://localhost:8084}")
    private String demographicServiceUrl;

    private String base() {
        return demographicServiceUrl + "/api/demographics";
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
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

    @GetMapping("/{userId}")
    public ResponseEntity<String> getByUserId(@PathVariable String userId) {
        return forward(HttpMethod.GET, base() + "/" + userId, null);
    }

    @PostMapping
    public ResponseEntity<String> upsert(@RequestBody String body) {
        return forward(HttpMethod.POST, base(), body);
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> bulk(@RequestBody String body) {
        return forward(HttpMethod.POST, base() + "/bulk", body);
    }

    private ResponseEntity<String> forward(HttpMethod method, String url, String body) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(body, jsonHeaders());
            return relay(restTemplate.exchange(url, method, entity, String.class));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Demographic proxy error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
