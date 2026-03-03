package com.mike.transfer.client;

import com.mike.transfer.config.FeignClientConfig;
import com.mike.transfer.dto.CardDto;
import com.mike.transfer.dto.LinkAccountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "card-service",
        url = "${card-service.url}",
        configuration = FeignClientConfig.class
)
public interface CardClient {

    @GetMapping("/cards/{cardId}")
    CardDto getCard(@PathVariable UUID cardId);

    @GetMapping("/cards/{userId}/user")
    CardDto[] getCardsByUser(@PathVariable UUID userId);

    @PatchMapping("/cards/{cardId}/account")
    void linkAccount(
            @PathVariable UUID cardId,
            @RequestBody LinkAccountRequest request
    );
}

