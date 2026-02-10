package com.mike.transfer.controller;

import com.mike.transfer.common.ApiError;
import com.mike.transfer.domain.Account;
import com.mike.transfer.dto.AccountResponse;
import com.mike.transfer.dto.TransferRequest;
import com.mike.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @Operation(summary = "Make transfer between cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer completed"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient funds or business conflict",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/transfers")
    public void makeTransfer(@Valid @RequestBody TransferRequest request,
                             @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        service.transfer(request, idempotencyKey);
    }

    @Operation(summary = "Account top-up")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account topped up",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/accounts/{accountId}/top-up")
    public BigDecimal topUp(
            @PathVariable UUID accountId,
            @RequestParam @Positive BigDecimal amount,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return service.topUp(accountId, amount, idempotencyKey);
    }

    @Operation(summary = "Account withdraw")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdraw completed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient funds",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/accounts/{accountId}/withdraw")
    public BigDecimal withdraw(
            @PathVariable UUID accountId,
            @RequestParam @Positive BigDecimal amount,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return service.withdraw(accountId, amount, idempotencyKey);
    }

    @Operation(summary = "Account get balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/accounts/{accountId}/balance")
    public BigDecimal getBalance(@PathVariable UUID accountId) {
        return service.getBalance(accountId);
    }

    @Operation(summary = "Get account by user id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{userId}/account")
    public AccountResponse getAccountByUserId(@PathVariable UUID userId) {
        Account account = service.getAccountByUserId(userId);
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getCurrency().name(),
                account.getBalance()
        );
    }
}
