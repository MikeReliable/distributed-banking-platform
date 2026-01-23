package com.mike.transfer.service;

import com.mike.transfer.domain.TransferRequest;
import com.mike.transfer.error.ErrorType;
import com.mike.transfer.error.TransferException;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    public void transfer(TransferRequest request) {
        if (request.fromCardId().equals(request.toCardId())) {
            throw new TransferException(ErrorType.VALIDATION_ERROR,
                    "Cannot transfer to the same card");
        }

        System.out.printf("Transferring %s from %s to %s%n",
                request.amount(), request.fromCardId(), request.toCardId());
    }
}
