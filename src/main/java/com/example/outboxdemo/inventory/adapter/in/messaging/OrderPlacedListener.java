package com.example.outboxdemo.inventory.adapter.in.messaging;

import com.example.outboxdemo.inventory.application.port.in.ReserveStockUseCase;
import com.example.outboxdemo.inventory.application.port.in.ReserveStockUseCase.Line;
import com.example.outboxdemo.inventory.application.port.in.ReserveStockUseCase.ReserveStockCommand;
import com.example.outboxdemo.outbox.inbox.InboxGuard;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

@Component
class OrderPlacedListener {

    static final String QUEUE = "inventory.order-placed";

    // Inventory's own, minimal view of the OrderPlaced event. It does NOT import the order
    // module's class: modules share a message contract, not code.
    record OrderPlacedMessage( UUID orderId, String customerEmail, List<LineMessage> lines ) {
    }

    record LineMessage( String sku, int quantity ) {
    }

    private final ReserveStockUseCase reserveStockUseCase;
    private final InboxGuard inboxGuard;
    private final JsonMapper jsonMapper;

    OrderPlacedListener( ReserveStockUseCase reserveStockUseCase, InboxGuard inboxGuard, JsonMapper jsonMapper ) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.inboxGuard = inboxGuard;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener( queues = QUEUE )
    void onOrderPlaced( Message message ) {
        var messageId = UUID.fromString( message.getMessageProperties().getMessageId() );
        var event = jsonMapper.readValue( message.getBody(), OrderPlacedMessage.class );
        var lines = event.lines().stream().map( l -> new Line( l.sku(), l.quantity() ) ).toList();

        inboxGuard.runOnce( QUEUE, messageId,
                () -> reserveStockUseCase.execute( new ReserveStockCommand( event.orderId(), event.customerEmail(), lines ) ) );
    }

}
