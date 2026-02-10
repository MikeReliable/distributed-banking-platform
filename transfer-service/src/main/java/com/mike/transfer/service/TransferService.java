package com.mike.transfer.service;

import com.mike.transfer.domain.Account;
import com.mike.transfer.domain.Currency;
import com.mike.transfer.domain.IdempotentRequest;
import com.mike.transfer.domain.Transfer;
import com.mike.transfer.dto.TransferRequest;
import com.mike.transfer.exception.AccountNotFoundException;
import com.mike.transfer.exception.CurrencyMismatchException;
import com.mike.transfer.exception.InvalidAmountException;
import com.mike.transfer.exception.SameAccountTransferException;
import com.mike.transfer.repository.AccountRepository;
import com.mike.transfer.repository.IdempotentRepository;
import com.mike.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final IdempotentRepository idempotentRepository;
    private final CardResolverService cardResolverService;

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

        if (idempotencyKey != null) {
            return idempotentRepository.findById(idempotencyKey)
                    .map(IdempotentRequest::getEntityId)
                    .orElseGet(() -> execute(fromAccountId, toAccountId, request.amount(), idempotencyKey));
        }

        return execute(fromAccountId, toAccountId, request.amount(), null);
    }

    private UUID execute(UUID from, UUID to, BigDecimal amount, String idempotencyKey) {
        validateAmount(amount);

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

        source.debit(amount);
        target.credit(amount);

        UUID transferId = UUID.randomUUID();
        transferRepository.save(new Transfer(transferId, from, to, amount));

        if (idempotencyKey != null) {
            idempotentRepository.save(new IdempotentRequest(idempotencyKey, transferId));
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

        if (idempotencyKey != null) {
            var existing = idempotentRepository.findById(idempotencyKey);
            if (existing.isPresent()) {
                return getBalance(accountId);
            }
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        account.credit(amount);

        if (idempotencyKey != null) {
            idempotentRepository.save(new IdempotentRequest(idempotencyKey, accountId));
        }

        log.info(
                "TopUp completed | accountId={} | amount={}",
                accountId, amount
        );

        return account.getBalance();
    }

    @Transactional
    public BigDecimal withdraw(UUID accountId, BigDecimal amount, String idempotencyKey) {
        validateAmount(amount);

        if (idempotencyKey != null) {
            var existing = idempotentRepository.findById(idempotencyKey);
            if (existing.isPresent()) {
                return getBalance(accountId);
            }
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        account.debit(amount);

        if (idempotencyKey != null) {
            idempotentRepository.save(new IdempotentRequest(idempotencyKey, accountId));
        }

        log.info(
                "Withdraw completed | accountId={} | amount={}",
                accountId, amount
        );

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
}
