package com.mike.transfer.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic cardEventsTopic() {
        return TopicBuilder.name("card-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic cardEventsDltTopic() {
        return TopicBuilder.name("card-events.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
