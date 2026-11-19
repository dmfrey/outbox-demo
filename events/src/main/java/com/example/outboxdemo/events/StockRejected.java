package com.example.outboxdemo.events;

import java.util.UUID;

/** Published when an order could not be reserved (insufficient stock). */
public record StockRejected( UUID orderId, String customerEmail ) {

    public static final String ROUTING_KEY = "inventory.stock-rejected";

}
