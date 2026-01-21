package com.mike.card.controller;

import com.mike.card.domain.Card;
import com.mike.card.service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CardController {

    private static final Logger log = LoggerFactory.getLogger(CardController.class);
    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @GetMapping
    public List<Card> myCards(
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Fetching cards for user={}", userId);
        return service.getCardsForUser(userId);
    }
}
