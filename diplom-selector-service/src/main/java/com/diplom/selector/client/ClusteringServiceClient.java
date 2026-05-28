package com.diplom.selector.client;

import com.diplom.selector.domain.model.UserAggregateState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ClusteringServiceClient {

    private final String clusteringServiceUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClusteringServiceClient(@Value("${app.clustering-service-url:http://localhost:8085}") String clusteringServiceUrl) {
        this.clusteringServiceUrl = clusteringServiceUrl;
    }

    public String assignVariant(String userId, UserAggregateState aggregateState) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("userId", userId);

            Map<String, Double> features = new HashMap<>();
            features.put("visitCount7Days", (double) aggregateState.getVisitCount7Days());
            features.put("purchaseCount", (double) aggregateState.getPurchaseCount());
            features.put("totalSpent", aggregateState.getTotalSpent());
            features.put("cartAddCount", (double) aggregateState.getCartAddCount());
            features.put("productViewCount", (double) aggregateState.getProductViewCount());
            features.put("cartAbandoned", aggregateState.isPurchasedAfterLastCart() ? 0.0 : 1.0);
            features.put("daysSinceLastEvent", calculateDaysSinceLastEvent(aggregateState.getLastEventTimestamp()));
            features.put("hoursSinceLastCart", calculateHoursSinceLastCart(aggregateState.getLastCartAddTimestamp()));

            request.put("features", features);

            String body = objectMapper.writeValueAsString(request);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(clusteringServiceUrl + "/api/cluster/assign"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                Map<String, Object> responseBody = objectMapper.readValue(resp.body(), Map.class);
                String variant = (String) responseBody.get("variant");
                Integer clusterId = ((Number) responseBody.get("clusterId")).intValue();
                Double distance = ((Number) responseBody.get("distance")).doubleValue();
                log.debug("User {} assigned to cluster {} variant {} (distance={})", userId, clusterId, variant, distance);
                return variant;
            } else {
                log.warn("Clustering service returned {}, falling back to random", resp.statusCode());
            }
        } catch (Exception e) {
            log.warn("Clustering service unavailable ({}), falling back to random assignment", e.getMessage());
        }

        return Math.random() < 0.5 ? "A" : "B";
    }

    private double calculateDaysSinceLastEvent(Long lastEventTimestamp) {
        if (lastEventTimestamp == null) return 365.0;
        long daysDiff = (System.currentTimeMillis() - lastEventTimestamp) / (86_400_000L);
        return Math.min(daysDiff, 365.0);
    }

    private double calculateHoursSinceLastCart(Long lastCartAddTimestamp) {
        if (lastCartAddTimestamp == null) return 730.0;
        long hoursDiff = (System.currentTimeMillis() - lastCartAddTimestamp) / (3_600_000L);
        return Math.min(hoursDiff, 730.0);
    }
}
