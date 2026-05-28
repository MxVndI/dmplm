package com.diplom.notification.utils;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopUserClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.shop-url:http://app:8080}")
    private String shopUrl;

    @Value("${app.test-service-url:http://test-service:8081}")
    private String testServiceUrl;

    public UserInfo getUser(String userId) {
        try {
            ResponseEntity<UserInfo> resp = restTemplate.exchange(
                    shopUrl + "/internal/users/" + userId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return resp.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch user {} from shop: {}", userId, e.getMessage());
            return null;
        }
    }

    public List<UserInfo> getAllUsers() {
        try {
            ResponseEntity<List<UserInfo>> resp = restTemplate.exchange(
                    shopUrl + "/internal/users",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            List<UserInfo> users = resp.getBody();
            return users != null ? users : List.of();
        } catch (Exception e) {
            log.error("Failed to fetch users from shop: {}", e.getMessage());
            return List.of();
        }
    }

    public List<String> getTestParticipants(String testId, String variant) {
        try {
            String url = testServiceUrl + "/api/tests/" + testId + "/participants";
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
            );
            List<Map<String, Object>> participants = resp.getBody();
            if (participants == null) return List.of();
            return participants.stream()
                    .filter(p -> variant == null || variant.isBlank()
                            || variant.equalsIgnoreCase((String) p.get("variant")))
                    .map(p -> (String) p.get("userId"))
                    .filter(id -> id != null)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch participants for test {}: {}", testId, e.getMessage());
            return List.of();
        }
    }

    public List<String> getUsersByTest(String testId, String variant) {
        return getTestParticipants(testId, variant);
    }

    public List<String> getAllUserIds() {
        List<UserInfo> users = getAllUsers();
        return users.stream().map(UserInfo::getId).toList();
    }

    @Data
    public static class UserInfo {
        private String id;
        private String login;
        private String email;
        private String firstName;
        private String lastName;
        private String telegramChatId;
    }
}
