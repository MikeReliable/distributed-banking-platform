package com.mike.card.controller;

import com.mike.card.domain.Card;
import com.mike.card.service.CardService;
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

    @GetMapping
    public List<Card> myCards(@RequestHeader("X-User-Id") UUID userId) {
        log.info("Fetching cards for user={}", userId);
        return service.getCardsForUser(userId);
    }

    @GetMapping(path = "/{cardId}")
    public Card getCardById(@PathVariable UUID cardId) {
        return service.getById(cardId);
    }

    @PostMapping("/{id}/block")
    public void block(@PathVariable UUID id) {
        service.block(id);
    }

    @PostMapping("/{id}/close")
    public void close(@PathVariable UUID id) {
        service.close(id);
    }
}
