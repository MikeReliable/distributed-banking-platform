package com.mike.card.controller;

import com.mike.card.dto.CardResponse;
import com.mike.card.dto.LinkAccountRequest;
import com.mike.card.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/cards")
public class CardController {

    private static final Logger log = LoggerFactory.getLogger(CardController.class);
    private final CardService service;

    @Operation(summary = "Get user cards")
    @GetMapping
    public List<CardResponse> myCards(@RequestHeader("X-User-Id") UUID userId) {
        return service.getCardsForUser(userId);
    }

    @Operation(summary = "Get card by id")
    @GetMapping(path = "/{cardId}")
    public CardResponse getCardById(@PathVariable UUID cardId) {
        return service.getById(cardId);
    }

    @Operation(summary = "Block card by id")
    @PostMapping("/{cardId}/block")
    public void block(@PathVariable UUID cardId) {
        service.block(cardId);
    }

    @Operation(summary = "Close card by id")
    @PostMapping("/{cardId}/close")
    public void close(@PathVariable UUID cardId) {
        service.close(cardId);
    }

    @Operation(summary = "Link card with account")
    @PatchMapping("/{cardId}/account")
    public void linkAccount(@PathVariable UUID cardId,
                            @Valid @RequestBody LinkAccountRequest request) {
        service.linkAccount(cardId, request.accountId());
    }
}
