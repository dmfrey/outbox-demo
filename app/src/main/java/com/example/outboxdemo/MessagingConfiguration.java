package com.example.outboxdemo;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;

/**
 * Everything RabbitMQ-specific about this deployment lives here, in the application - not in the
 * shared events library and not in the modules.
 */
@Configuration( proxyBeanMethods = false )
class MessagingConfiguration {

    static final String EXCHANGE = "domain.events";

    @Bean
    TopicExchange domainEventsExchange() {
        return ExchangeBuilder.topicExchange( EXCHANGE ).durable( true ).build();
    }

    /** Externalised events leave as JSON rather than Java-serialised bytes. */
    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Which events leave the monolith, and where they go. Because the event records are plain Java
     * (no {@code @Externalized}), the routing is declared here, in code.
     */
    @Bean
    EventExternalizationConfiguration eventExternalization() {
        return EventExternalizationConfiguration.externalizing()
                .selectByPackage( OrderPlaced.class )
                .route( OrderPlaced.class, e -> RoutingTarget.forTarget( EXCHANGE ).andKey( OrderPlaced.ROUTING_KEY ) )
                .route( StockReserved.class, e -> RoutingTarget.forTarget( EXCHANGE ).andKey( StockReserved.ROUTING_KEY ) )
                .route( StockRejected.class, e -> RoutingTarget.forTarget( EXCHANGE ).andKey( StockRejected.ROUTING_KEY ) )
                .build();
    }

}
