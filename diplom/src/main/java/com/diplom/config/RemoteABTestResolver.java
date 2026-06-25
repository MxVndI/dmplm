package com.diplom.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class RemoteABTestResolver implements ABTestResolver {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.ab-rule-service-url:}")
    private String abRuleServiceUrl;

    @Value("${app.test-service-admin-username:admin}")
    private String adminUsername;

    @Value("${app.test-service-admin-password:}")
    private String adminPassword;

    @Override
    public Optional<ABResolution> resolve(HttpServletRequest request, String userId) {
        if (abRuleServiceUrl == null || abRuleServiceUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            String path = request.getRequestURI();
            String url = UriComponentsBuilder
                    .fromUriString(abRuleServiceUrl + "/api/ab/resolve")
                    .queryParam("userId", userId)
                    .queryParam("path", path)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            String credentials = adminUsername + ":" + adminPassword;
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> body = response.getBody();
                String abTestId = (String) body.get("abTestId");
                String variant  = (String) body.get("variant");
                if (abTestId != null && variant != null) {
                    return Optional.of(new ABResolution(abTestId, variant));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("AB rule service unavailable for userId={} path={} ({}); using default template.",
                    userId, request.getRequestURI(), e.getMessage());
            return Optional.empty();
        }
    }
}
