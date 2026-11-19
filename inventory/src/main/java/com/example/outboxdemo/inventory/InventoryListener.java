package com.example.outboxdemo.inventory;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** The module's edge: translates shared events to and from inventory's own domain types. */
@Component
class InventoryListener {

    private final InventoryService inventory;
    private final ApplicationEventPublisher events;

    InventoryListener( InventoryService inventory, ApplicationEventPublisher events ) {
        this.inventory = inventory;
        this.events = events;
    }

    // Runs in its own transaction, so the stock update and the outcome event commit together.
    @ApplicationModuleListener
    void on( OrderPlaced event ) {
        var request = new ReservationRequest( event.orderId(),
                event.lines().stream().map( l -> new ReservationRequest.Item( l.sku(), l.quantity() ) ).toList() );

        switch ( inventory.reserve( request ) ) {
            // The customer email is not inventory's concern; it is passed through so downstream
            // consumers (notification) need not call back to find it.
            case RESERVED -> events.publishEvent( new StockReserved( event.orderId(), event.customerEmail() ) );
            case INSUFFICIENT_STOCK -> events.publishEvent( new StockRejected( event.orderId(), event.customerEmail() ) );
        }
    }

}
