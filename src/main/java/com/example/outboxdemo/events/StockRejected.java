package com.example.outboxdemo.events;

import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized( "domain.events::inventory.stock-rejected" )
public record StockRejected( UUID orderId, String customerEmail ) {
}
