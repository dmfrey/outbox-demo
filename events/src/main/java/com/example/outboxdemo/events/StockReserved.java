package com.example.outboxdemo.events;

import java.util.UUID;

/** Published when every line of an order could be reserved. Carries the customer email so consumers need no lookup. */
public record StockReserved( UUID orderId, String customerEmail ) {

    public static final String ROUTING_KEY = "inventory.stock-reserved";

}
