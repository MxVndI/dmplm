package com.diplom.config;

import com.diplom.event.TestParticipantEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ── Topics ──────────────────────────────────────────────────────────────

    @Bean
    public NewTopic userProfilesTopic() {
        return TopicBuilder.name("user-profiles").partitions(3).replicas(1)
                .config("cleanup.policy", "compact")
                .config("min.cleanable.dirty.ratio", "0.01")
                .config("segment.ms", "60000")
                .build();
    }

    @Bean
    public NewTopic testParticipantsResultTopic() {
        return TopicBuilder.name("test-participants-result").partitions(3).replicas(1).build();
    }

    /** Behavioural events consumed by diplom-selector-service for stream segmentation. */
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events").partitions(3).replicas(1).build();
    }

    /** Segment-change events published by diplom-selector-service. */
    @Bean
    public NewTopic userSegmentChangesTopic() {
        return TopicBuilder.name("user-segment-changes").partitions(3).replicas(1).build();
    }

    // ── Consumer factory for TestParticipantEvent ───────────────────────────

    @Bean
    public ConsumerFactory<String, TestParticipantEvent> participantConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "diplom-shop-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<TestParticipantEvent> valueDeseriaSzer =
                new JsonDeserializer<>(TestParticipantEvent.class, false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeseriaSzer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TestParticipantEvent>
    participantKafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, TestParticipantEvent>();
        factory.setConsumerFactory(participantConsumerFactory());
        return factory;
    }

    // ── Producer template for UserProfileEvent (user-profiles topic) ────────

    @Bean
    public ProducerFactory<String, com.diplom.event.UserProfileEvent> profileProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, com.diplom.event.UserProfileEvent> kafkaTemplate() {
        return new KafkaTemplate<>(profileProducerFactory());
    }

    // ── Producer template for generic events (user-events topic) ──────────────

    @Bean
    public ProducerFactory<String, Object> eventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "eventKafkaTemplate")
    public KafkaTemplate<String, Object> eventKafkaTemplate() {
        return new KafkaTemplate<>(eventProducerFactory());
    }
}
