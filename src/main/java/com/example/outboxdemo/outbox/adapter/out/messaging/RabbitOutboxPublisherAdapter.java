package com.example.outboxdemo.outbox.adapter.out.messaging;

import com.example.outboxdemo.outbox.application.domain.model.OutboxMessage;
import com.example.outboxdemo.outbox.application.port.out.PublishOutboxMessagesPort;
import com.example.outboxdemo.outbox.configuration.OutboxMessagingConfiguration;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
class RabbitOutboxPublisherAdapter implements PublishOutboxMessagesPort {

    private final RabbitTemplate rabbitTemplate;

    RabbitOutboxPublisherAdapter( RabbitTemplate rabbitTemplate ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish( List<OutboxMessage> messages ) {
        // invoke() pins every send to ONE channel so we can wait for the broker's confirms.
        rabbitTemplate.invoke( operations -> {
            for ( var m : messages ) {
                var message = MessageBuilder
                        .withBody( m.payload().getBytes( StandardCharsets.UTF_8 ) )
                        .setContentType( MessageProperties.CONTENT_TYPE_JSON )
                        // The outbox row id travels with the message: it is the idempotency key.
                        .setMessageId( m.id().toString() )
                        .setType( m.eventType() )
                        .build();
                operations.send( OutboxMessagingConfiguration.EVENTS_EXCHANGE, m.eventType(), message );
            }
            operations.waitForConfirmsOrDie( 5_000 );
            return null;
        } );
    }

}
