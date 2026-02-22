package com.mike.card.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic createdEventsTopic() {
        return TopicBuilder.name("created-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic createdEventsDltTopic() {
        return TopicBuilder.name("created-events.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
