package com.example.outboxdemo;

import com.example.outboxdemo.order.OrderLine;
import com.example.outboxdemo.order.OrderManagement;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** LESSON 2: @Externalized events also reach RabbitMQ, for consumers outside this application. */
class ExternalizationTest extends AbstractIntegrationTest {

    @Autowired OrderManagement orders;
    @Autowired AmqpAdmin amqpAdmin;
    @Autowired RabbitTemplate rabbitTemplate;

    @Test
    void orderPlacedIsForwardedToTheBroker() {
        var queue = QueueBuilder.durable( "test.external-consumer" ).build();
        amqpAdmin.declareQueue( queue );
        amqpAdmin.declareBinding( BindingBuilder.bind( queue ).to( new TopicExchange( "domain.events" ) ).with( "order.placed" ) );

        var order = orders.place( "dave@example.com", List.of( new OrderLine( "BOOK-1", 1 ) ) );

        var message = rabbitTemplate.receive( "test.external-consumer", 20_000 );
        assertThat( message ).isNotNull();
        assertThat( new String( message.getBody() ) ).contains( order.id().toString() ).contains( "dave@example.com" );
    }

}
