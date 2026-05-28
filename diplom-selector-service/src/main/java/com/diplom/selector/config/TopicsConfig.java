package com.diplom.selector.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class TopicsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic userProfilesTopic() {
        return TopicBuilder.name("user-profiles").partitions(3).replicas(1)
                .config("cleanup.policy", "compact")
                .build();
    }

    @Bean
    public NewTopic userSegmentChangesTopic() {
        return TopicBuilder.name("user-segment-changes").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic testSelectionRequestsTopic() {
        return TopicBuilder.name("test-selection-requests").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic testParticipantsResultTopic() {
        return TopicBuilder.name("test-participants-result").partitions(3).replicas(1).build();
    }
}
