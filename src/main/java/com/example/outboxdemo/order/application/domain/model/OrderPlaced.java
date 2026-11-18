package com.example.outboxdemo.order.application.domain.model;

import java.util.List;
import java.util.UUID;

/** Domain event: this is what ends up (as JSON) in the outbox and then on the broker. */
public record OrderPlaced( UUID orderId, String customerEmail, List<OrderLine> lines ) {

    public static final String EVENT_TYPE = "order.placed";

    public static OrderPlaced from( Order order ) {
        return new OrderPlaced( order.id(), order.customerEmail(), order.lines() );
    }

}
