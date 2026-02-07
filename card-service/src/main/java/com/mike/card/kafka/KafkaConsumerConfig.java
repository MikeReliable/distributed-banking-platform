package com.mike.card.kafka;

import com.mike.card.event.UserCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    private final ConsumerFactory<String, UserCreatedEvent> consumerFactory;

    public KafkaConsumerConfig(ConsumerFactory<String, UserCreatedEvent> consumerFactory) {
        this.consumerFactory = consumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> kafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler());
        return factory;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        DefaultErrorHandler handler = new DefaultErrorHandler(
                (ConsumerRecord<?, ?> record, Exception ex) -> {
                    log.error(
                            "Kafka message skipped. topic={}, partition={}, offset={}",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            ex
                    );
                },
                new FixedBackOff(0L, 0)
        );

        handler.addNotRetryableExceptions(DeserializationException.class);

        return handler;
    }
}
