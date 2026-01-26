package com.mike.card.controller;

import com.mike.card.domain.Card;
import com.mike.card.service.CardService;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/cards")
public class CardController {

    private static final Logger log = LoggerFactory.getLogger(CardController.class);
    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @GetMapping
    public List<Card> myCards(@RequestHeader("X-User-Id") @NotBlank String userId) {
        log.info("Fetching cards for user={}", userId);
        return service.getCardsForUser(userId);
    }

    @GetMapping(path = "/{cardId}")
    public Card getCardById(@PathVariable @NotBlank UUID cardId) {
        return service.getById(cardId);
    }
}
