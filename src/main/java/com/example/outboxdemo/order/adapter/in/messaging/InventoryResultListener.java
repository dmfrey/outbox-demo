package com.example.outboxdemo.order.adapter.in.messaging;

import com.example.outboxdemo.order.application.domain.model.OrderStatus;
import com.example.outboxdemo.order.application.port.in.UpdateOrderStatusUseCase;
import com.example.outboxdemo.order.application.port.in.UpdateOrderStatusUseCase.UpdateOrderStatusCommand;
import com.example.outboxdemo.outbox.inbox.InboxGuard;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

/** Closes the loop: the order module learns the outcome of the stock reservation. */
@Component
class InventoryResultListener {

    static final String QUEUE = "order.stock-results";
    private static final String CONSUMER = "order.stock-results";

    // This module's own view of the event: it only declares the fields it needs.
    record StockResult( UUID orderId ) {
    }

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final InboxGuard inboxGuard;
    private final JsonMapper jsonMapper;

    InventoryResultListener( UpdateOrderStatusUseCase updateOrderStatusUseCase, InboxGuard inboxGuard, JsonMapper jsonMapper ) {
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.inboxGuard = inboxGuard;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener( queues = QUEUE )
    void onStockResult( Message message ) {
        var messageId = UUID.fromString( message.getMessageProperties().getMessageId() );
        var type = message.getMessageProperties().getType();
        var result = jsonMapper.readValue( message.getBody(), StockResult.class );
        var status = "inventory.stock-reserved".equals( type ) ? OrderStatus.CONFIRMED : OrderStatus.REJECTED;

        inboxGuard.runOnce( CONSUMER, messageId,
                () -> updateOrderStatusUseCase.execute( new UpdateOrderStatusCommand( result.orderId(), status ) ) );
    }

}
