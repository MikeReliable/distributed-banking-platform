package com.mike.transfer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.transfer.dto.CardCreatedEvent;
import com.mike.transfer.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(CardCreatedConsumer.class);

    private final TransferService transferService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "card-events", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(
            @Payload String message,
            @Header(name = "X-Request-Id", required = false) String requestId
    ) throws JsonProcessingException {

        if (message == null || message.isBlank()) {
            log.warn("Kafka message empty");
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(message);

            JsonNode typeNode = root.get("type");
            JsonNode payloadNode = root.get("payload");

            if (typeNode == null || payloadNode == null) {
                log.warn("Invalid event format");
                return;
            }

            if (!"CARD_CREATED".equals(root.get("type").asText())) {
                log.debug("Kafka event ignored | type={}", root.get("type").asText());
                return;
            }

            CardCreatedEvent event =
                    objectMapper.treeToValue(root.get("payload"), CardCreatedEvent.class);

            if (requestId == null) {
                requestId = "kafka-" + UUID.randomUUID();
            }
            MDC.put("requestId", requestId);

            if (event.userId() == null) {
                log.warn("CARD_CREATED without userId");
                return;
            }

            log.info("CARD_CREATED event received | userId={}", event.userId());
            transferService.createDefaultAccounts(UUID.fromString(event.userId()));
            log.info("CARD_CREATED event processed | userId={}", event.userId());

        } catch (Exception e) {
            log.error("Kafka event processing failed", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
