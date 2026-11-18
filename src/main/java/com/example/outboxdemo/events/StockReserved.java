package com.example.outboxdemo.events;

import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized( "domain.events::inventory.stock-reserved" )
public record StockReserved( UUID orderId, String customerEmail ) {
}
