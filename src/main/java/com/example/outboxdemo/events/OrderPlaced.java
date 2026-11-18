package com.example.outboxdemo.events;

import org.springframework.modulith.events.Externalized;

import java.util.List;
import java.util.UUID;

/**
 * Published when an order is placed. Spring Modulith stores it in the event publication registry
 * (the outbox) in the publisher's transaction, delivers it to each module's listener, and - because
 * of {@code @Externalized} - also forwards it to RabbitMQ ({@code exchange::routing-key}).
 */
@Externalized( "domain.events::order.placed" )
public record OrderPlaced( UUID orderId, String customerEmail, List<Line> lines ) {

    public record Line( String sku, int quantity ) {
    }

}
