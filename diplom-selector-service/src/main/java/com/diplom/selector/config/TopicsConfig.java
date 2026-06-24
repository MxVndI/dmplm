package com.diplom.selector.config;

import com.diplom.selector.constant.AppConstants;
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
        return TopicBuilder.name(AppConstants.TOPIC_USER_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic userProfilesTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_USER_PROFILES).partitions(3).replicas(1)
                .config(AppConstants.CLEANUP_POLICY_CONFIG, AppConstants.CLEANUP_POLICY_COMPACT)
                .build();
    }

    @Bean
    public NewTopic userSegmentChangesTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_SEGMENT_CHANGES).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic testSelectionRequestsTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_SELECTION_REQUESTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic testParticipantsResultTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_PARTICIPANTS_RESULT).partitions(3).replicas(1).build();
    }
}
