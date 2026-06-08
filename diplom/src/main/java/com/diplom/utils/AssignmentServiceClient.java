package com.diplom.utils;

import com.diplom.domain.service.ABTestService;
import com.diplom.persistance.entity.UserTestParticipationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceClient {

    private final ABTestService abTestService;
    private final RestTemplate assignmentRestTemplate = new RestTemplate();

    @Value("${app.assignment-service-url:}")
    private String assignmentServiceUrl;

    public Optional<UserTestParticipationEntity> getAssignment(String userId) {
        if (assignmentServiceUrl != null && !assignmentServiceUrl.isBlank()) {
            try {
                String url = assignmentServiceUrl + "/api/assignments/" + userId;
                @SuppressWarnings("unchecked")
                ResponseEntity<Map> response =
                        assignmentRestTemplate.getForEntity(url, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<?, ?> body = response.getBody();
                    String testId = (String) body.get("testId");
                    if (testId == null || testId.isBlank()) {
                        return Optional.empty();
                    }
                    UserTestParticipationEntity p = new UserTestParticipationEntity();
                    p.setUserId(userId);
                    p.setTestId(testId);
                    p.setVariant((String) body.get("variant"));
                    p.setEnrolledAt(LocalDateTime.now());
                    log.debug("Assignment from remote service: userId={} testId={} variant={}",
                            userId, p.getTestId(), p.getVariant());
                    return Optional.of(p);
                }
                return Optional.empty();
            } catch (HttpClientErrorException.NotFound e) {
                return Optional.empty();
            } catch (Exception e) {
                log.debug("Assignment service unavailable ({}); falling back to local lookup.", e.getMessage());
            }
        }
        return abTestService.findActiveParticipation(userId);
    }
}
