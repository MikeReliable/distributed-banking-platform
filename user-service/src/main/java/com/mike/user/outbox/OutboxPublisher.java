package com.mike.user.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publish() {

        var events = outboxRepository
                .findTop10ByPublishedFalseOrderByCreatedAt();

        for (OutboxEvent event : events) {
            try {
                ObjectNode root = objectMapper.createObjectNode();
                root.put("type", event.getType());
                root.put("aggregateId", event.getAggregateId());
                root.set("payload", event.getPayload());

                String payload = objectMapper.writeValueAsString(root);
                ProducerRecord<String, String> record =
                        new ProducerRecord<>("user-events", event.getAggregateId(), payload);

                if (event.getRequestId() != null) {
                    record.headers().add(
                            "X-Request-Id",
                            event.getRequestId().getBytes(StandardCharsets.UTF_8)
                    );
                }
                kafkaTemplate.send(record);

                event.markPublished();
                log.info(
                        "Outbox event published | type={} | aggregateId={}",
                        event.getType(),
                        event.getAggregateId()
                );
            } catch (Exception e) {
                log.error(
                        "Outbox publish failed | eventId={} | type={}",
                        event.getId(),
                        event.getType(),
                        e
                );
            }
        }
    }
}
