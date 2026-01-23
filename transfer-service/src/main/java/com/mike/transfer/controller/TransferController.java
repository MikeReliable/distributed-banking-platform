package com.mike.transfer.controller;

import com.mike.transfer.domain.TransferRequest;
import com.mike.transfer.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);
    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping
    public void makeTransfer(@Valid @RequestBody TransferRequest request) {
        log.info("Transfer request: from={} to={} amount={}",
                request.fromCardId(), request.toCardId(), request.amount());
        service.transfer(request);
    }
}
