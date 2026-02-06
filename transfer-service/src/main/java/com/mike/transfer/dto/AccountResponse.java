package com.mike.transfer.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID userId,
        String currency,
        BigDecimal balance
) {}
