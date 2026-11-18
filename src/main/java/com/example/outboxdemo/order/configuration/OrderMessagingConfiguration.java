package com.example.outboxdemo.order.configuration;

import com.example.outboxdemo.outbox.configuration.OutboxMessagingConfiguration;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration( proxyBeanMethods = false )
class OrderMessagingConfiguration {

    @Bean
    Declarables orderTopology() {
        var queue = OutboxMessagingConfiguration.consumerQueue( "order.stock-results" );
        var exchange = OutboxMessagingConfiguration.eventsExchange();
        return new Declarables( queue,
                BindingBuilder.bind( queue ).to( exchange ).with( "inventory.stock-reserved" ),
                BindingBuilder.bind( queue ).to( exchange ).with( "inventory.stock-rejected" ) );
    }

}
