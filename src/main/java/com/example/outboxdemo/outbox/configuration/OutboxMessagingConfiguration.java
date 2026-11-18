package com.example.outboxdemo.outbox.configuration;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.core.TopicExchange;

@Configuration( proxyBeanMethods = false )
@EnableScheduling
public class OutboxMessagingConfiguration {

    /** All domain events go to this topic exchange; the routing key is the event type. */
    public static final String EVENTS_EXCHANGE = "domain.events";
    public static final String DEAD_LETTER_EXCHANGE = "domain.events.dlx";
    public static final String DEAD_LETTER_QUEUE = "domain.events.dead-letters";

    @Bean
    Declarables outboxTopology() {
        var events = ExchangeBuilder.topicExchange( EVENTS_EXCHANGE ).durable( true ).build();
        var dlx = new FanoutExchange( DEAD_LETTER_EXCHANGE );
        var dlq = QueueBuilder.durable( DEAD_LETTER_QUEUE ).build();
        return new Declarables( events, dlx, dlq, BindingBuilder.bind( dlq ).to( dlx ) );
    }

    /** Helper for consumer modules: a durable queue that dead-letters rejected messages. */
    public static Queue consumerQueue( String name ) {
        return QueueBuilder.durable( name ).deadLetterExchange( DEAD_LETTER_EXCHANGE ).build();
    }

    public static TopicExchange eventsExchange() {
        return new TopicExchange( EVENTS_EXCHANGE );
    }

}
