package com.example.outboxdemo.events;

import java.util.List;
import java.util.UUID;

/** Published when an order is placed. A plain record: the contract, nothing else. */
public record OrderPlaced( UUID orderId, String customerEmail, List<Line> lines ) {

    /** Broker routing key. Part of the contract, so producers and consumers cannot drift apart. */
    public static final String ROUTING_KEY = "order.placed";

    public record Line( String sku, int quantity ) {
    }

}
