package com.mike.user.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.user.dto.UserRegisteredEvent;
import com.mike.user.service.UserService;
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
public class UserRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "registered-events", groupId = "user-service-group")
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

            if (!"USER_REGISTERED".equals(root.get("type").asText())) {
                log.debug("Kafka event ignored | type={}", root.get("type").asText());
                return;
            }

            UserRegisteredEvent event =
                    objectMapper.treeToValue(root.get("payload"), UserRegisteredEvent.class);

            if (requestId == null) {
                requestId = "kafka-" + UUID.randomUUID();
            }
            MDC.put("requestId", requestId);

            if (event.userId() == null) {
                log.warn("USER_REGISTERED without userId");
                return;
            }

            log.info("USER_REGISTERED event received | userId={}", event.userId());
            userService.createUser(event);
            log.info("USER_REGISTERED event processed | userId={}", event.userId());

        } catch (Exception e) {
            log.error("Kafka event processing failed", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
