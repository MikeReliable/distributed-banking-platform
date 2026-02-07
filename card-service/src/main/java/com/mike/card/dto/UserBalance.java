package com.mike.card.dto;

import java.math.BigDecimal;
import java.util.UUID;

public interface UserBalance {
    UUID getUserId();
    UUID getAccountId();
    BigDecimal getBalance();
    String getCurrency();
    int getCardsCount();
}
