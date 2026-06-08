package com.diplom.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class RemoteABTestResolver implements ABTestResolver {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.ab-rule-service-url:}")
    private String abRuleServiceUrl;

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

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

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
            log.debug("AB rule service unavailable ({}); using default template.", e.getMessage());
            return Optional.empty();
        }
    }
}
