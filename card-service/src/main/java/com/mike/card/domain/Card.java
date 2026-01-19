package com.mike.card.domain;

public record Card(
        String id,
        String userId,
        String number,
        String currency
) {}
