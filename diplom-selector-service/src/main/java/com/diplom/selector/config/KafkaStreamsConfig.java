package com.diplom.selector.config;

import com.diplom.selector.client.ClusteringServiceClient;
import com.diplom.selector.constant.AppConstants;
import com.diplom.selector.domain.model.SelectionRequest;
import com.diplom.selector.domain.model.UserAggregateState;
import com.diplom.selector.domain.model.UserEvent;
import com.diplom.selector.domain.model.UserProfile;
import com.diplom.selector.event.TestParticipantEvent;
import com.diplom.selector.event.UserSegmentChangedEvent;
import com.diplom.selector.serde.JsonSerde;
import com.diplom.selector.stream.SegmentEvaluator;
import com.diplom.selector.stream.UserSelectionProcessor;
import com.diplom.selector.stream.UserStateTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaStreamsConfig {

    public static final String AGGREGATE_STORE = AppConstants.AGGREGATE_STORE;

    private final SegmentEvaluator segmentEvaluator;
    private final ClusteringServiceClient clusteringServiceClient;

    @Value("${app.windows.visit-days:7}")
    private int visitWindowDays;

    @Value("${app.demographic-service-url:http://localhost:8084}")
    private String demographicServiceUrl;

    @Value("${app.selector.fail-open-on-demographic-error:true}")
    private boolean failOpenOnDemographicError;

    @Bean
    public StreamsBuilder kStreamsTopology(StreamsBuilder builder) {

        JsonSerde<UserEvent>               userEventSerde   = new JsonSerde<>(UserEvent.class);
        JsonSerde<UserProfile>             userProfileSerde = new JsonSerde<>(UserProfile.class);
        JsonSerde<UserAggregateState>      stateSerde       = new JsonSerde<>(UserAggregateState.class);
        JsonSerde<UserSegmentChangedEvent> changedSerde     = new JsonSerde<>(UserSegmentChangedEvent.class);
        JsonSerde<SelectionRequest>        requestSerde     = new JsonSerde<>(SelectionRequest.class);
        JsonSerde<TestParticipantEvent>    participantSerde = new JsonSerde<>(TestParticipantEvent.class);

        long windowMillis = (long) visitWindowDays * AppConstants.MILLIS_PER_DAY;


        StoreBuilder<KeyValueStore<String, UserAggregateState>> aggregateStore =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(AppConstants.AGGREGATE_STORE),
                        Serdes.String(),
                        stateSerde);
        builder.addStateStore(aggregateStore);

        KStream<String, UserEvent> behavioralEvents = builder.stream(
                AppConstants.TOPIC_USER_EVENTS,
                Consumed.with(Serdes.String(), userEventSerde));

        KStream<String, UserSegmentChangedEvent> segmentChanges = behavioralEvents.process(
                () -> new UserStateTransformer(segmentEvaluator, AppConstants.AGGREGATE_STORE, windowMillis),
                Named.as("segment-transformer"),
                AppConstants.AGGREGATE_STORE);

        segmentChanges.to(AppConstants.TOPIC_SEGMENT_CHANGES, Produced.with(Serdes.String(), changedSerde));


        builder.globalTable(
                AppConstants.TOPIC_USER_PROFILES,
                Consumed.with(Serdes.String(), userProfileSerde),
                Materialized.<String, UserProfile, KeyValueStore<Bytes, byte[]>>as(AppConstants.USER_PROFILES_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(userProfileSerde));

        KStream<String, SelectionRequest> selectionRequests = builder.stream(
                AppConstants.TOPIC_SELECTION_REQUESTS,
                Consumed.with(Serdes.String(), requestSerde));

        KStream<String, TestParticipantEvent> participants = selectionRequests.process(
                () -> new UserSelectionProcessor(
                        AppConstants.USER_PROFILES_STORE,
                        demographicServiceUrl,
                        failOpenOnDemographicError,
                        clusteringServiceClient,
                        AppConstants.AGGREGATE_STORE),
                Named.as("user-selection"),
                AppConstants.AGGREGATE_STORE);

        participants.to(AppConstants.TOPIC_PARTICIPANTS_RESULT, Produced.with(Serdes.String(), participantSerde));

        log.info("Streams topology built: " +
                "[A] {{}, {}} → state-store '{}' → {} | " +
                "[B] {} + globalTable({}) → {}",
                AppConstants.TOPIC_USER_EVENTS, AppConstants.TOPIC_USER_PROFILES, AppConstants.AGGREGATE_STORE, AppConstants.TOPIC_SEGMENT_CHANGES,
                AppConstants.TOPIC_SELECTION_REQUESTS, AppConstants.TOPIC_USER_PROFILES, AppConstants.TOPIC_PARTICIPANTS_RESULT);

        return builder;
    }
}
