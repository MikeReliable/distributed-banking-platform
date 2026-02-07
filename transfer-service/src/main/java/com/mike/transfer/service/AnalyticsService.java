package com.mike.transfer.service;

import com.mike.transfer.dto.AccountTurnover;
import com.mike.transfer.dto.TransferDto;
import com.mike.transfer.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AccountTurnover getAccountTurnover(UUID accountId, Instant from, Instant to) {
        return analyticsRepository.turnover(accountId, from, to);
    }

    public List<TransferDto> getTopTransfers(UUID accountId, Instant from, int limit) {
        return analyticsRepository.topTransfers(accountId, from, limit);
    }
}
