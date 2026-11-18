package com.example.outboxdemo.notification.configuration;

import com.example.outboxdemo.outbox.configuration.OutboxMessagingConfiguration;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration( proxyBeanMethods = false )
class NotificationMessagingConfiguration {

    @Bean
    Declarables notificationTopology() {
        var queue = OutboxMessagingConfiguration.consumerQueue( "notification.order-events" );
        var exchange = OutboxMessagingConfiguration.eventsExchange();
        return new Declarables( queue,
                BindingBuilder.bind( queue ).to( exchange ).with( "order.placed" ),
                BindingBuilder.bind( queue ).to( exchange ).with( "inventory.stock-reserved" ),
                BindingBuilder.bind( queue ).to( exchange ).with( "inventory.stock-rejected" ) );
    }

}
