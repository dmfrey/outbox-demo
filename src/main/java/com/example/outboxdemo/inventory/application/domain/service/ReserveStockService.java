package com.example.outboxdemo.inventory.application.domain.service;

import com.example.outboxdemo.inventory.application.port.in.ReserveStockUseCase;
import com.example.outboxdemo.inventory.application.port.out.RecordStockRejectedPort;
import com.example.outboxdemo.inventory.application.port.out.RecordStockReservedPort;
import com.example.outboxdemo.inventory.application.port.out.ReserveStockPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ReserveStockService implements ReserveStockUseCase {

    private final ReserveStockPort reserveStockPort;
    private final RecordStockReservedPort recordStockReservedPort;
    private final RecordStockRejectedPort recordStockRejectedPort;

    ReserveStockService(
            ReserveStockPort reserveStockPort,
            RecordStockReservedPort recordStockReservedPort,
            RecordStockRejectedPort recordStockRejectedPort
    ) {
        this.reserveStockPort = reserveStockPort;
        this.recordStockReservedPort = recordStockReservedPort;
        this.recordStockRejectedPort = recordStockRejectedPort;
    }

    // Inventory is both a consumer (of OrderPlaced) and a producer (of the stock result), so it
    // uses the outbox too: the stock update and its outcome event commit together.
    @Override
    @Transactional
    public void execute( ReserveStockCommand command ) {
        if ( reserveStockPort.reserve( command.lines() ) ) {
            recordStockReservedPort.record( command.orderId(), command.customerEmail() );
        } else {
            recordStockRejectedPort.record( command.orderId(), command.customerEmail() );
        }
    }

}
