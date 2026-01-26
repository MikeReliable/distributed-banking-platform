package com.mike.card.kafka;

import com.mike.card.event.UserCreatedEvent;
import com.mike.card.service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedConsumer.class);

    private final CardService cardService;

    public UserCreatedConsumer(CardService cardService) {
        this.cardService = cardService;
    }

    @KafkaListener(topics = "user-created", groupId = "card-service-group")
    public void listen(@Payload(required = false) UserCreatedEvent event) {

        if (event == null) {
            log.warn("Received empty USER_CREATED event, skipping");
            return;
        }

        String requestId = "kafka-" + event.userId();

        MDC.put("requestId", requestId);
        try {
            log.info("Received USER_CREATED event: {}", event);
            cardService.createDefaultCards(event.userId());
        } finally {
            MDC.clear();
        }
    }
}
