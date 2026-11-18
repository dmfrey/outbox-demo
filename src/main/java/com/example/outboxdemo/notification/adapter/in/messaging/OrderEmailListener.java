package com.example.outboxdemo.notification.adapter.in.messaging;

import com.example.outboxdemo.notification.application.port.in.SendOrderEmailUseCase;
import com.example.outboxdemo.notification.application.port.in.SendOrderEmailUseCase.Kind;
import com.example.outboxdemo.notification.application.port.in.SendOrderEmailUseCase.SendOrderEmailCommand;
import com.example.outboxdemo.outbox.inbox.InboxGuard;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

/**
 * Notification knows nothing about orders or inventory internals - it just reacts to events.
 * Note: if the SMTP call throws, the transaction in InboxGuard rolls back (the message is NOT
 * marked processed), the listener retries, and after max attempts the message is dead-lettered.
 */
@Component
class OrderEmailListener {

    static final String QUEUE = "notification.order-events";

    record EventMessage( UUID orderId, String customerEmail ) {
    }

    private final SendOrderEmailUseCase sendOrderEmailUseCase;
    private final InboxGuard inboxGuard;
    private final JsonMapper jsonMapper;

    OrderEmailListener( SendOrderEmailUseCase sendOrderEmailUseCase, InboxGuard inboxGuard, JsonMapper jsonMapper ) {
        this.sendOrderEmailUseCase = sendOrderEmailUseCase;
        this.inboxGuard = inboxGuard;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener( queues = QUEUE )
    void onEvent( Message message ) {
        var messageId = UUID.fromString( message.getMessageProperties().getMessageId() );
        var event = jsonMapper.readValue( message.getBody(), EventMessage.class );
        var kind = switch ( message.getMessageProperties().getType() ) {
            case "order.placed" -> Kind.RECEIVED;
            case "inventory.stock-reserved" -> Kind.CONFIRMED;
            case "inventory.stock-rejected" -> Kind.BACKORDERED;
            default -> throw new IllegalArgumentException( "Unexpected event type " + message.getMessageProperties().getType() );
        };

        inboxGuard.runOnce( QUEUE, messageId,
                () -> sendOrderEmailUseCase.execute( new SendOrderEmailCommand( event.orderId(), event.customerEmail(), kind ) ) );
    }

}
