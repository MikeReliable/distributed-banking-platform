package com.mike.transfer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.transfer.event.CardCreatedEvent;
import com.mike.transfer.repository.AccountRepository;
import com.mike.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(CardCreatedConsumer.class);

    private final TransferService transferService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "card-events", groupId = "transfer-service-group")
    public void listen(@Payload String message) throws JsonProcessingException {

        if (message == null || message.isBlank()) {
            log.warn("Received empty Kafka message, skipping");
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(message);

            JsonNode typeNode = root.get("type");
            JsonNode payloadNode = root.get("payload");

            if (typeNode == null || payloadNode == null) {
                log.warn("Invalid event format: {}", message);
                return;
            }

            if (!"CARD_CREATED".equals(root.get("type").asText())) {
                log.debug("Ignoring event type={}", root.get("type").asText());
                return;
            }

            CardCreatedEvent event =
                    objectMapper.treeToValue(root.get("payload"), CardCreatedEvent.class);

            String requestId = "kafka-" + event.userId();
            MDC.put("requestId", requestId);

            if (event.userId() == null) {
                log.warn("CARD_CREATED without userId, skipping");
                return;
            }

            log.info("Received CARD_CREATED event: {}", event);
            transferService.createDefaultAccounts(UUID.fromString(event.userId()));
            log.info("CARD_CREATED processed successfully userId={}", event.userId());

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
