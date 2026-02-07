package com.mike.transfer.dto;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountTurnover {
    UUID getAccountId();
    String getCurrency();
    Long getOperationsCount();
    BigDecimal getTurnover();
}
