package com.mike.user.domain;

import lombok.Getter;

@Getter
public enum OutboxEventTopic {
    USER_CREATED("created-events"),
    USER_BLOCKED("blocked-events");

    private final String topicName;

    OutboxEventTopic(String topicName) {
        this.topicName = topicName;
    }

    public static OutboxEventTopic fromType(String type) {
        return valueOf(type);
    }
}
