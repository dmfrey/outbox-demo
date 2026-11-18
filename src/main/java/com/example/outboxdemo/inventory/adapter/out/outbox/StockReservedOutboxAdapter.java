package com.example.outboxdemo.inventory.adapter.out.outbox;

import com.example.outboxdemo.inventory.application.port.out.RecordStockReservedPort;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase.RecordOutboxMessageCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class StockReservedOutboxAdapter implements RecordStockReservedPort {

    record StockReserved( UUID orderId, String customerEmail ) {
    }

    private final RecordOutboxMessageUseCase recordOutboxMessageUseCase;

    StockReservedOutboxAdapter( RecordOutboxMessageUseCase recordOutboxMessageUseCase ) {
        this.recordOutboxMessageUseCase = recordOutboxMessageUseCase;
    }

    @Override
    public void record( UUID orderId, String customerEmail ) {
        recordOutboxMessageUseCase.execute( new RecordOutboxMessageCommand(
                "Order", orderId.toString(), "inventory.stock-reserved", new StockReserved( orderId, customerEmail ) ) );
    }

}
