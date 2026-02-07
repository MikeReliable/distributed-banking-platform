package com.mike.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String fromCardId,
        @NotBlank String toCardId,
        @NotNull @Positive BigDecimal amount
) {}
