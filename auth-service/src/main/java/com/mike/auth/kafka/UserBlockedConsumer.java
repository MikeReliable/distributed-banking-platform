package com.mike.auth.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.auth.dto.UserBlockedEvent;
import com.mike.auth.service.AuthService;
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
public class UserBlockedConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserBlockedConsumer.class);

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "blocked-events", groupId = "${spring.kafka.consumer.group-id}")
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

            if (!"USER_BLOCKED".equals(root.get("type").asText())) {
                log.debug("Kafka event ignored | type={}", root.get("type").asText());
                return;
            }

            UserBlockedEvent event =
                    objectMapper.treeToValue(root.get("payload"), UserBlockedEvent.class);

            if (requestId == null) {
                requestId = "kafka-" + UUID.randomUUID();
            }
            MDC.put("requestId", requestId);

            if (event.userId() == null) {
                log.warn("USER_BLOCKED without userId");
                return;
            }

            log.info("USER_BLOCKED event received | userId={}", event.userId());
            authService.blockUser(UUID.fromString(event.userId()));
            log.info("USER_BLOCKED event processed | userId={}", event.userId());

        } catch (Exception e) {
            log.error("Kafka event processing failed", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
