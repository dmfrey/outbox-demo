package com.example.outboxdemo.inventory.adapter.out.outbox;

import com.example.outboxdemo.inventory.application.port.out.RecordStockRejectedPort;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase.RecordOutboxMessageCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class StockRejectedOutboxAdapter implements RecordStockRejectedPort {

    record StockRejected( UUID orderId, String customerEmail ) {
    }

    private final RecordOutboxMessageUseCase recordOutboxMessageUseCase;

    StockRejectedOutboxAdapter( RecordOutboxMessageUseCase recordOutboxMessageUseCase ) {
        this.recordOutboxMessageUseCase = recordOutboxMessageUseCase;
    }

    @Override
    public void record( UUID orderId, String customerEmail ) {
        recordOutboxMessageUseCase.execute( new RecordOutboxMessageCommand(
                "Order", orderId.toString(), "inventory.stock-rejected", new StockRejected( orderId, customerEmail ) ) );
    }

}
