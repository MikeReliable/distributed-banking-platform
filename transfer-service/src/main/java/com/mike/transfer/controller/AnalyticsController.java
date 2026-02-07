package com.mike.transfer.controller;

import com.mike.transfer.dto.AccountTurnover;
import com.mike.transfer.dto.TransferDto;
import com.mike.transfer.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Account turnover for the period (native SQL)")
    @GetMapping("/accounts/{accountId}/turnover")
    public AccountTurnover getTurnover(
            @PathVariable UUID accountId,
            @Schema(
                    description = "Start of the period",
                    defaultValue = "2026-01-01T00:00:00Z"
            )
            @RequestParam Instant from,
            @Schema(
                    description = "End of the period",
                    defaultValue = "2026-06-01T00:00:00Z"
            )
            @RequestParam Instant to
    ) {
        return analyticsService.getAccountTurnover(accountId, from, to);
    }

    @Operation(summary = "Account top N transfers for the period (native SQL)")
    @GetMapping("/transfers/{accountId}/top-transfers")
    public List<TransferDto> getTopTransfers(
            @PathVariable UUID accountId,
            @Schema(
                    description = "Start of the period",
                    defaultValue = "2026-01-01T00:00:00Z"
            )
            @RequestParam Instant from,
            @Schema(
                    description = "Number of operations",
                    defaultValue = "3"
            )
            @RequestParam int limit
    ) {
        return analyticsService.getTopTransfers(accountId, from, limit);
    }
}
