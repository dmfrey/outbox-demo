package com.example.outboxdemo.notificationservice;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** This service owns its queue and says which events it wants - the monolith knows nothing about it. */
@Configuration( proxyBeanMethods = false )
class AmqpTopology {

    @Bean
    Declarables notificationServiceTopology() {
        var exchange = new TopicExchange( "domain.events", true, false );
        var queue = QueueBuilder.durable( AmqpEventListener.QUEUE ).build();
        return new Declarables( exchange, queue,
                BindingBuilder.bind( queue ).to( exchange ).with( OrderPlaced.ROUTING_KEY ),
                BindingBuilder.bind( queue ).to( exchange ).with( StockReserved.ROUTING_KEY ),
                BindingBuilder.bind( queue ).to( exchange ).with( StockRejected.ROUTING_KEY ) );
    }

}
