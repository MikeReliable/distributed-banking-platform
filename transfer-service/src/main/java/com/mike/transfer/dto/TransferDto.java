package com.mike.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface TransferDto {
    UUID getId();
    UUID getFromAccount();
    UUID getToAccount();
    BigDecimal getAmount();
    Instant getTransactionAt();
}
