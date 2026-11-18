package com.example.outboxdemo.inventory.configuration;

import com.example.outboxdemo.outbox.configuration.OutboxMessagingConfiguration;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration( proxyBeanMethods = false )
class InventoryMessagingConfiguration {

    @Bean
    Declarables inventoryTopology() {
        var queue = OutboxMessagingConfiguration.consumerQueue( "inventory.order-placed" );
        return new Declarables( queue,
                BindingBuilder.bind( queue ).to( OutboxMessagingConfiguration.eventsExchange() ).with( "order.placed" ) );
    }

}
