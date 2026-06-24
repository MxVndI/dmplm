package com.diplom.config;

import com.diplom.constant.AppConstants;
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

    @Bean
    public NewTopic userProfilesTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_USER_PROFILES).partitions(3).replicas(1)
                .config(AppConstants.CLEANUP_POLICY_CONFIG, AppConstants.CLEANUP_POLICY_COMPACT)
                .config(AppConstants.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, AppConstants.MIN_CLEANABLE_DIRTY_RATIO)
                .config(AppConstants.SEGMENT_MS_CONFIG, AppConstants.SEGMENT_MS)
                .build();
    }

    @Bean
    public NewTopic testParticipantsResultTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_PARTICIPANTS_RESULT).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_USER_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic userSegmentChangesTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_SEGMENT_CHANGES).partitions(3).replicas(1).build();
    }

    @Bean
    public ConsumerFactory<String, TestParticipantEvent> participantConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, AppConstants.SHOP_GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AppConstants.AUTO_OFFSET_EARLIEST);

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
