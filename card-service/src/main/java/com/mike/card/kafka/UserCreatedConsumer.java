package com.mike.card.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.card.event.UserCreatedEvent;
import com.mike.card.service.CardService;
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
public class UserCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedConsumer.class);

    private final CardService cardService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-events", groupId = "card-service-group")
    public void listen(@Payload String message) throws JsonProcessingException {

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

            if (!"USER_CREATED".equals(root.get("type").asText())) {
                log.debug("Kafka event ignored | type={}", root.get("type").asText());
                return;
            }

            UserCreatedEvent event =
                    objectMapper.treeToValue(root.get("payload"), UserCreatedEvent.class);

            String requestId = "kafka-" + event.userId();
            MDC.put("requestId", requestId);

            if (event.userId() == null) {
                log.warn("USER_CREATED without userId");
                return;
            }

            log.info("UserCreated event received | userId={}", event.userId());
            cardService.createDefaultCards(UUID.fromString(event.userId()));
            log.info("UserCreated event processed | userId={}", event.userId());

        } catch (Exception e) {
            log.error("Kafka event processing failed", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
