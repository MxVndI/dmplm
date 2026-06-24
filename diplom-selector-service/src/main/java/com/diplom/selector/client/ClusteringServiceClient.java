package com.diplom.selector.client;

import com.diplom.selector.constant.AppConstants;
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

    public ClusterResult assignCluster(String userId, UserAggregateState aggregateState) {
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
                    .uri(URI.create(clusteringServiceUrl + AppConstants.CLUSTER_ASSIGN_PATH))
                    .header(AppConstants.CONTENT_TYPE, AppConstants.APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                Map<String, Object> responseBody = objectMapper.readValue(resp.body(), Map.class);
                Integer clusterId = ((Number) responseBody.get("clusterId")).intValue();
                Double distance = ((Number) responseBody.get("distance")).doubleValue();
                log.debug("User {} assigned to cluster {} (distance={})", userId, clusterId, distance);
                return new ClusterResult(clusterId, distance);
            } else {
                log.warn("Clustering service returned {}, using fallback cluster", resp.statusCode());
            }
        } catch (Exception e) {
            log.warn("Clustering service unavailable ({}), using fallback cluster", e.getMessage());
        }

        return new ClusterResult(AppConstants.FALLBACK_CLUSTER_ID, null);
    }

    private double calculateDaysSinceLastEvent(Long lastEventTimestamp) {
        if (lastEventTimestamp == null) return AppConstants.MAX_DAYS_SINCE_EVENT;
        long daysDiff = (System.currentTimeMillis() - lastEventTimestamp) / AppConstants.MILLIS_PER_DAY;
        return Math.min(daysDiff, AppConstants.MAX_DAYS_SINCE_EVENT);
    }

    private double calculateHoursSinceLastCart(Long lastCartAddTimestamp) {
        if (lastCartAddTimestamp == null) return AppConstants.MAX_HOURS_SINCE_CART;
        long hoursDiff = (System.currentTimeMillis() - lastCartAddTimestamp) / AppConstants.MILLIS_PER_HOUR;
        return Math.min(hoursDiff, AppConstants.MAX_HOURS_SINCE_CART);
    }

    public record ClusterResult(int clusterId, Double distance) {
    }
}
