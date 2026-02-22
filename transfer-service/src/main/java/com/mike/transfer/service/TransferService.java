package com.mike.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.transfer.domain.Account;
import com.mike.transfer.domain.Currency;
import com.mike.transfer.domain.IdempotentRequest;
import com.mike.transfer.domain.Transfer;
import com.mike.transfer.dto.TransferRequest;
import com.mike.transfer.exception.*;
import com.mike.transfer.repository.AccountRepository;
import com.mike.transfer.repository.IdempotentRepository;
import com.mike.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private record TopUpRequest(UUID accountId, BigDecimal amount) {
    }

    private record WithdrawRequest(UUID accountId, BigDecimal amount) {
    }

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final IdempotentRepository idempotentRepository;
    private final CardResolverService cardResolverService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createDefaultAccounts(UUID userId) {
        try {
            if (accountRepository.findByUserId(userId).isPresent()) {
                log.info("Account already exists | userId={} ", userId);
                return;
            }

            UUID accountId = UUID.randomUUID();
            accountRepository.save(new Account(
                    accountId,
                    userId,
                    Currency.USD
            ));

            cardResolverService.updateCardAccountInfo(userId);

            log.info(
                    "Account created | accountId={} | userId={} | currency={}",
                    accountId, userId, Currency.USD
            );
        } catch (DataIntegrityViolationException ex) {
            log.info("Account already exists (concurrent) | userId={} ", userId);
        }
    }

    @Transactional
    public UUID transfer(TransferRequest request, String idempotencyKey) {

        log.info(
                "Transfer started | fromCardId={} | toCardId={} | amount={}",
                request.fromCardId(), request.toCardId(), request.amount()
        );

        UUID fromAccountId = cardResolverService.getAccountId(UUID.fromString(request.fromCardId()));
        UUID toAccountId = cardResolverService.getAccountId(UUID.fromString(request.toCardId()));

        return handleIdempotency(
                idempotencyKey,
                request,
                "TRANSFER",
                () -> executeTransfer(fromAccountId, toAccountId, request, idempotencyKey),
                transferId -> transferId
        );
    }

    private UUID executeTransfer(UUID from, UUID to, TransferRequest request, String idempotencyKey) {
        validateAmount(request.amount());

        if (from.equals(to)) {
            throw new SameAccountTransferException();
        }

        Account source = accountRepository.findById(from)
                .orElseThrow(() -> new AccountNotFoundException(from));
        Account target = accountRepository.findById(to)
                .orElseThrow(() -> new AccountNotFoundException(to));

        if (!source.getCurrency().equals(target.getCurrency())) {
            throw new CurrencyMismatchException();
        }

        source.debit(request.amount());
        target.credit(request.amount());

        UUID transferId = UUID.randomUUID();
        transferRepository.save(new Transfer(transferId, from, to, request.amount()));

        if (idempotencyKey != null) {
            String hash = computeHash(request, "TRANSFER");
            idempotentRepository.save(new IdempotentRequest(idempotencyKey, transferId, hash));
        }

        log.info(
                "Transfer completed | transferId={} | fromAccount={} | toAccount={}",
                transferId, from, to
        );

        return transferId;
    }

    @Transactional
    public BigDecimal topUp(UUID accountId, BigDecimal amount, String idempotencyKey) {
        validateAmount(amount);
        var request = new TopUpRequest(accountId, amount);

        return handleIdempotency(
                idempotencyKey,
                request,
                "TOP_UP",
                () -> executeTopUp(accountId, amount, idempotencyKey, request),
                this::getBalance
        );
    }

    private BigDecimal executeTopUp(UUID accountId, BigDecimal amount, String idempotencyKey, TopUpRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        account.credit(amount);

        if (idempotencyKey != null) {
            String hash = computeHash(request, "TOP_UP");
            idempotentRepository.save(new IdempotentRequest(idempotencyKey, accountId, hash));
        }

        log.info("TopUp completed | accountId={} | amount={}", accountId, amount);
        return account.getBalance();
    }

    @Transactional
    public BigDecimal withdraw(UUID accountId, BigDecimal amount, String idempotencyKey) {
        validateAmount(amount);
        var request = new WithdrawRequest(accountId, amount);

        return handleIdempotency(
                idempotencyKey,
                request,
                "WITHDRAW",
                () -> executeWithdraw(accountId, amount, idempotencyKey, request),
                this::getBalance
        );
    }

    private BigDecimal executeWithdraw(UUID accountId, BigDecimal amount, String idempotencyKey, WithdrawRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        account.debit(amount);

        if (idempotencyKey != null) {
            String hash = computeHash(request, "WITHDRAW");
            idempotentRepository.save(new IdempotentRequest(idempotencyKey, accountId, hash));
        }

        log.info("Withdraw completed | accountId={} | amount={}", accountId, amount);
        return account.getBalance();
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(Account::getBalance)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException();
        }
    }

    @Transactional(readOnly = true)
    public Account getAccountByUserId(UUID userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new AccountNotFoundException(userId)
                );
    }

    private <T> T handleIdempotency(String key, Object request, String operationType,
                                    Supplier<T> action, Function<UUID, T> resultMapper) {
        if (key == null) {
            return action.get();
        }

        String currentHash = computeHash(request, operationType);
        var existing = idempotentRepository.findById(key);

        if (existing.isPresent()) {
            IdempotentRequest ir = existing.get();
            if (!currentHash.equals(ir.getRequestHash())) {
                throw new IdempotencyConflictException();
            }
            return resultMapper.apply(ir.getEntityId());
        }

        return action.get();
    }

    private String computeHash(Object request, String operationType) {
        try {
            Map<String, Object> map = objectMapper.convertValue(request, new TypeReference<>() {
            });
            map.put("operationType", operationType);
            String json = objectMapper.writeValueAsString(map);
            return DigestUtils.sha256Hex(json);
        } catch (JsonProcessingException e) {
            throw new IdempotencyHashException();
        }
    }
}
