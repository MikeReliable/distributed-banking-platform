package com.mike.transfer.service;

import com.mike.transfer.domain.Account;
import com.mike.transfer.domain.IdempotentRequest;
import com.mike.transfer.domain.Transfer;
import com.mike.transfer.domain.TransferRequest;
import com.mike.transfer.error.ErrorType;
import com.mike.transfer.error.TransferException;
import com.mike.transfer.repository.AccountRepository;
import com.mike.transfer.repository.IdempotentRepository;
import com.mike.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final IdempotentRepository idempotentRepository;

    public void transfer(TransferRequest request) {
        if (request.fromCardId().equals(request.toCardId())) {
            throw new TransferException(ErrorType.VALIDATION_ERROR,
                    "Cannot transfer to the same card");
        }

        System.out.printf("Transferring %s from %s to %s%n",
                request.amount(), request.fromCardId(), request.toCardId());
    }

    @Transactional
    public UUID transfer(
            UUID from,
            UUID to,
            BigDecimal amount,
            String idempotencyKey
    ) {
        if (idempotencyKey != null) {
            return idempotentRepository.findById(idempotencyKey)
                    .map(IdempotentRequest::getEntityId)
                    .orElseGet(() -> execute(from, to, amount, idempotencyKey));
        }
        return execute(from, to, amount, null);
    }

    private UUID execute(UUID from, UUID to, BigDecimal amount, String key) {

        Account source = accountRepository.findById(from).orElseThrow();
        Account target = accountRepository.findById(to).orElseThrow();

        source.debit(amount);
        target.credit(amount);

        UUID transferId = UUID.randomUUID();
        transferRepository.save(new Transfer(
                transferId, from, to, amount
        ));

        if (key != null) {
            idempotentRepository.save(
                    new IdempotentRequest(key, transferId)
            );
        }

        return transferId;
    }
}
