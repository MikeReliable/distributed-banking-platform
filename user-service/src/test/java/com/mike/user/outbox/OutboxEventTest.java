package com.mike.user.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {

    @Test
    void markPublished_setsPublishedTrue() {

        // given
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "User",
                "123",
                null,
                "USER_CREATED",
                new ObjectMapper().createObjectNode()
        );

        // when
        event.markPublished();

        // then
        assertTrue(event.isPublished());
    }

    @Test
    void markFailed_incrementsRetryCountAndSetsError() {

        // given
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "User",
                "123",
                null,
                "USER_CREATED",
                new ObjectMapper().createObjectNode()
        );

        // when
        event.markFailed("kafka timeout");

        // then
        assertEquals(1, event.getRetryCount());
        assertEquals("kafka timeout", event.getLastError());
    }

}