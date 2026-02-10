package com.mike.card.controller;

import com.mike.card.common.ApiError;
import com.mike.card.dto.CardResponse;
import com.mike.card.dto.LinkAccountRequest;
import com.mike.card.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/cards")
public class CardController {


    private final CardService service;

    @Operation(summary = "Get user cards")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cards found",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CardResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public List<CardResponse> myCards(@RequestHeader("X-User-Id") UUID userId) {
        return service.getCardsForUser(userId);
    }

    @Operation(summary = "Get card by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Card not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping(path = "/{cardId}")
    public CardResponse getCardById(@PathVariable UUID cardId) {
        return service.getById(cardId);
    }

    @Operation(summary = "Block card by id")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Card not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{cardId}/block")
    public void block(@PathVariable UUID cardId) {
        service.block(cardId);
    }

    @Operation(summary = "Close card by id")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Card not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{cardId}/close")
    public void close(@PathVariable UUID cardId) {
        service.close(cardId);
    }

    @Operation(summary = "Link card with account")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Card not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{cardId}/account")
    public void linkAccount(@PathVariable UUID cardId,
                            @Valid @RequestBody LinkAccountRequest request) {
        service.linkAccount(cardId, request.accountId());
    }
}
