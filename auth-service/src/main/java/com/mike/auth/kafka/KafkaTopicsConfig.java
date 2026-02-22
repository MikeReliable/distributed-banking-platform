package com.mike.auth.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic blockedEventsTopic() {
        return TopicBuilder.name("blocked-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic blockedEventsDltTopic() {
        return TopicBuilder.name("blocked-events.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
