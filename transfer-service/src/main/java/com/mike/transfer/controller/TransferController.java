package com.mike.transfer.controller;

import com.mike.transfer.domain.Account;
import com.mike.transfer.domain.TransferRequest;
import com.mike.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @Operation(summary = "Make transfer between cards")
    @PostMapping
    public void makeTransfer(@Valid @RequestBody TransferRequest request,
                             @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("Transfer request: from={} to={} amount={}",
                request.fromCardId(), request.toCardId(), request.amount());
        service.transfer(request, idempotencyKey);
    }

    @Operation(summary = "Account top-up")
    @PostMapping("/accounts/{accountId}/top-up")
    public BigDecimal topUp(
            @PathVariable UUID accountId,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return service.topUp(accountId, amount, idempotencyKey);
    }

    @Operation(summary = "Account withdraw")
    @PostMapping("/accounts/{accountId}/withdraw")
    public BigDecimal withdraw(
            @PathVariable UUID accountId,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return service.withdraw(accountId, amount, idempotencyKey);
    }

    @Operation(summary = "Account get balance")
    @GetMapping("/accounts/{accountId}/balance")
    public BigDecimal getBalance(@PathVariable UUID accountId) {
        return service.getBalance(accountId);
    }

    @Operation(summary = "Get account by user id")
    @GetMapping("/{userId}")
    public Account getAccountByUserId(@PathVariable UUID userId) {
        return service.getAccountByUserId(userId);
    }
}
