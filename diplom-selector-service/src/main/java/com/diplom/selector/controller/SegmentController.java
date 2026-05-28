package com.diplom.selector.controller;

import com.diplom.selector.config.KafkaStreamsConfig;
import com.diplom.selector.domain.model.UserAggregateState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/segments")
@RequiredArgsConstructor
public class SegmentController {

    private final StreamsBuilderFactoryBean streamsFactory;

    private KafkaStreams streams() {
        return streamsFactory.getKafkaStreams();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getSegment(@PathVariable String userId) {
        try {
            ReadOnlyKeyValueStore<String, UserAggregateState> store = streams().store(
                    StoreQueryParameters.fromNameAndType(
                            KafkaStreamsConfig.AGGREGATE_STORE,
                            QueryableStoreTypes.keyValueStore()
                    )
            );
            UserAggregateState state = store.get(userId);
            if (state == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("userId",             state.getUserId());
            body.put("segment",            state.getCurrentSegment());
            body.put("visitCount7Days",    state.getVisitCount7Days());
            body.put("productViewCount",   state.getProductViewCount());
            body.put("purchaseCount",      state.getPurchaseCount());
            body.put("totalSpent",         state.getTotalSpent());
            body.put("cartAddCount",       state.getCartAddCount());
            body.put("cartAbandoned",
                    state.getLastCartAddTimestamp() != null && !state.isPurchasedAfterLastCart());
            body.put("lastEventTimestamp", state.getLastEventTimestamp());
            body.put("segmentChangedAt",   state.getSegmentChangedAt());

            return ResponseEntity.ok(body);

        } catch (InvalidStateStoreException e) {
            log.warn("State store unavailable when querying userId={}: {}", userId, e.getMessage());
            return ResponseEntity.status(503).build();
        }
    }

    /** Health / readiness: returns the Kafka Streams state. */
    @GetMapping("/status")
    public Map<String, Object> status() {
        KafkaStreams ks = streams();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("streamsState", ks != null ? ks.state().toString() : "NOT_STARTED");
        body.put("running",      ks != null && ks.state() == KafkaStreams.State.RUNNING);
        return body;
    }
}
