package com.example.outboxdemo.order.adapter.out.outbox;

import com.example.outboxdemo.order.application.domain.model.OrderPlaced;
import com.example.outboxdemo.order.application.port.out.RecordOrderPlacedPort;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase.RecordOutboxMessageCommand;
import org.springframework.stereotype.Component;

@Component
class OrderOutboxAdapter implements RecordOrderPlacedPort {

    private final RecordOutboxMessageUseCase recordOutboxMessageUseCase;

    OrderOutboxAdapter( RecordOutboxMessageUseCase recordOutboxMessageUseCase ) {
        this.recordOutboxMessageUseCase = recordOutboxMessageUseCase;
    }

    @Override
    public void record( OrderPlaced event ) {
        recordOutboxMessageUseCase.execute( new RecordOutboxMessageCommand(
                "Order", event.orderId().toString(), OrderPlaced.EVENT_TYPE, event ) );
    }

}
