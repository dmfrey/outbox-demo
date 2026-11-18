package com.example.outboxdemo.inventory;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class InventoryListener {

    private final StockRepository stock;
    private final ApplicationEventPublisher events;

    InventoryListener( StockRepository stock, ApplicationEventPublisher events ) {
        this.stock = stock;
        this.events = events;
    }

    // Inventory is both a listener (of OrderPlaced) and a publisher (of the stock result). The
    // listener runs in its own transaction, so the stock update and the outcome event commit
    // together - the outbox guarantee again, for free.
    @ApplicationModuleListener
    void on( OrderPlaced event ) {
        if ( stock.reserve( event.lines() ) ) {
            events.publishEvent( new StockReserved( event.orderId(), event.customerEmail() ) );
        } else {
            events.publishEvent( new StockRejected( event.orderId(), event.customerEmail() ) );
        }
    }

}
