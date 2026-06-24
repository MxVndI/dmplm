package com.diplom.selector.stream;

import com.diplom.selector.client.ClusteringServiceClient;
import com.diplom.selector.constant.AppConstants;
import com.diplom.selector.domain.model.DemographicProfile;
import com.diplom.selector.domain.model.SelectionRequest;
import com.diplom.selector.domain.model.TestCriteria;
import com.diplom.selector.domain.model.UserAggregateState;
import com.diplom.selector.domain.model.UserProfile;
import com.diplom.selector.event.TestParticipantEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UserSelectionProcessor
        extends ContextualProcessor<String, SelectionRequest, String, TestParticipantEvent> {

    private final String storeName;
    private final String demographicServiceUrl;
    private final boolean failOpenOnDemographicError;
    private final ClusteringServiceClient clusteringClient;
    private final String aggregateStoreName;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ReadOnlyKeyValueStore<String, ValueAndTimestamp<UserProfile>> userStore;
    private ReadOnlyKeyValueStore<String, UserAggregateState> aggregateStore;

    public UserSelectionProcessor(String storeName,
                                  String demographicServiceUrl,
                                  boolean failOpenOnDemographicError,
                                  ClusteringServiceClient clusteringClient,
                                  String aggregateStoreName) {
        this.storeName = storeName;
        this.demographicServiceUrl = demographicServiceUrl;
        this.failOpenOnDemographicError = failOpenOnDemographicError;
        this.clusteringClient = clusteringClient;
        this.aggregateStoreName = aggregateStoreName;
    }

    @Override
    public void init(ProcessorContext<String, TestParticipantEvent> context) {
        super.init(context);
        this.userStore = context.getStateStore(storeName);
        this.aggregateStore = context.getStateStore(aggregateStoreName);
        log.info("UserSelectionProcessor initialised, userStore='{}', aggregateStore='{}'", storeName, aggregateStoreName);
    }

    @Override
    public void process(Record<String, SelectionRequest> record) {
        SelectionRequest request = record.value();
        if (request == null) return;

        log.info("Processing selection request for test '{}' (id={})", request.getTestName(), request.getTestId());

        TestCriteria criteria = request.getCriteria();
        List<String> matching = new ArrayList<>();

        try (KeyValueIterator<String, ValueAndTimestamp<UserProfile>> iter = userStore.all()) {
            while (iter.hasNext()) {
                KeyValue<String, ValueAndTimestamp<UserProfile>> kv = iter.next();
                UserProfile profile = kv.value.value();
                if (profile != null && matchesProfile(profile, criteria)) {
                    matching.add(kv.key);
                }
            }
        }
        log.info("Profile-store matched {} users for test '{}'", matching.size(), request.getTestId());

        if (matching.isEmpty()) {
            matching = fetchAllUserIds();
            log.info("Fallback fetched {} users from demographic-service", matching.size());
        }

        if (hasDemographicCriteria(criteria) && !matching.isEmpty()) {
            matching = applyDemographicFilter(matching, criteria);
            log.info("After demographic filtering: {} users for test '{}'", matching.size(), request.getTestId());
        }

        matching.sort(String::compareTo);

        if (criteria != null && criteria.getMaxParticipants() != null && criteria.getMaxParticipants() > 0) {
            int limit = Math.min(matching.size(), criteria.getMaxParticipants());
            matching = matching.subList(0, limit);
        }

        Instant now = Instant.now();
        List<AssignmentCandidate> candidates = matching.stream()
                .map(userId -> new AssignmentCandidate(userId, assignCluster(userId)))
                .sorted(Comparator
                        .comparingInt(AssignmentCandidate::clusterId)
                        .thenComparing(AssignmentCandidate::userId))
                .toList();

        Map<Integer, VariantBalance> clusterBalances = new HashMap<>();
        for (AssignmentCandidate candidate : candidates) {
            VariantBalance balance = clusterBalances.computeIfAbsent(candidate.clusterId(), key -> new VariantBalance());
            String variant = chooseVariant(request.getTestId(), candidate.userId(), candidate.clusterId(), balance);
            balance.accept(variant);

            TestParticipantEvent event = new TestParticipantEvent(
                    request.getTestId(), candidate.userId(), variant, candidate.clusterId(), now);
            context().forward(new Record<>(request.getTestId(), event, record.timestamp()));
        }
        log.info("Forwarded {} participant events for test '{}' (stratified by k-means clusters)", matching.size(), request.getTestId());
    }

    private boolean matchesProfile(UserProfile p, TestCriteria c) {
        if (c == null) return true;
        if (c.getAgeMin() != null && (p.getAge() == null || p.getAge() < c.getAgeMin())) return false;
        if (c.getAgeMax() != null && (p.getAge() == null || p.getAge() > c.getAgeMax())) return false;
        if (nonEmpty(c.getCountries()) && !c.getCountries().contains(p.getCountry())) return false;
        if (nonEmpty(c.getLanguages()) && !c.getLanguages().contains(p.getLanguage())) return false;
        if (nonEmpty(c.getGenders()) && !c.getGenders().contains(p.getGender())) return false;
        return true;
    }

    private boolean hasDemographicCriteria(TestCriteria c) {
        return c != null && (nonEmpty(c.getIncomeLevels()) || nonEmpty(c.getEducationLevels())
                || nonEmpty(c.getOccupations()) || nonEmpty(c.getInterests()));
    }

    private List<String> applyDemographicFilter(List<String> userIds, TestCriteria c) {
        try {
            String body = objectMapper.writeValueAsString(userIds);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(demographicServiceUrl + "/api/demographics/bulk"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new IllegalStateException("demographic-service returned " + resp.statusCode());
            }
            List<DemographicProfile> demographics = objectMapper.readValue(resp.body(), new TypeReference<>() {});
            Map<String, DemographicProfile> byUserId = demographics.stream()
                    .filter(d -> d.getUserId() != null)
                    .collect(Collectors.toMap(DemographicProfile::getUserId, d -> d, (a, b) -> a));

            return userIds.stream()
                    .filter(uid -> matchesDemographics(byUserId.get(uid), c))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            if (failOpenOnDemographicError) {
                log.warn("Demographic filter skipped (fail-open): {}", e.getMessage());
                return userIds;
            }
            log.warn("Demographic filter failed (fail-closed): {}", e.getMessage());
            return List.of();
        }
    }

    private boolean matchesDemographics(DemographicProfile d, TestCriteria c) {
        if (d == null) return false;
        if (nonEmpty(c.getIncomeLevels()) && !c.getIncomeLevels().contains(d.getIncomeLevel())) return false;
        if (nonEmpty(c.getEducationLevels()) && !c.getEducationLevels().contains(d.getEducationLevel())) return false;
        if (nonEmpty(c.getOccupations()) && !c.getOccupations().contains(d.getOccupation())) return false;
        if (nonEmpty(c.getInterests())) {
            if (d.getInterests() == null || d.getInterests().isEmpty()) return false;
            Set<String> required = new HashSet<>(c.getInterests());
            if (d.getInterests().stream().noneMatch(required::contains)) return false;
        }
        return true;
    }

    private List<String> fetchAllUserIds() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(demographicServiceUrl + "/api/demographics"))
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) return List.of();
            List<DemographicProfile> all = objectMapper.readValue(resp.body(), new TypeReference<>() {});
            return all.stream()
                    .map(DemographicProfile::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to fetch all users from demographic-service: {}", e.getMessage());
            return List.of();
        }
    }

    private boolean nonEmpty(List<String> l) { return l != null && !l.isEmpty(); }

    private int assignCluster(String userId) {
        try {
            UserAggregateState state = aggregateStore.get(userId);
            if (state != null) {
                return clusteringClient.assignCluster(userId, state).clusterId();
            }
        } catch (Exception e) {
            log.warn("Error assigning cluster for user {}: {}", userId, e.getMessage());
        }
        return AppConstants.FALLBACK_CLUSTER_ID;
    }

    private String chooseVariant(String testId, String userId, int clusterId, VariantBalance balance) {
        if (balance.variantA < balance.variantB) return AppConstants.VARIANT_A;
        if (balance.variantB < balance.variantA) return AppConstants.VARIANT_B;

        int hash = Objects.hash(testId, userId, clusterId);
        return Math.floorMod(hash, 2) == 0 ? AppConstants.VARIANT_A : AppConstants.VARIANT_B;
    }

    private record AssignmentCandidate(String userId, int clusterId) {
    }

    private static final class VariantBalance {
        private long variantA;
        private long variantB;

        private void accept(String variant) {
            if (AppConstants.VARIANT_A.equals(variant)) {
                variantA++;
            } else {
                variantB++;
            }
        }
    }
}
