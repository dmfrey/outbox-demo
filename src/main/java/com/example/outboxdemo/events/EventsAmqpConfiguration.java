package com.example.outboxdemo.events;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration( proxyBeanMethods = false )
class EventsAmqpConfiguration {

    /** Target of every {@code @Externalized( "domain.events::..." )} event. */
    @Bean
    TopicExchange domainEventsExchange() {
        return ExchangeBuilder.topicExchange( "domain.events" ).durable( true ).build();
    }

    /** Externalised events leave as JSON rather than Java-serialised bytes. */
    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

}
