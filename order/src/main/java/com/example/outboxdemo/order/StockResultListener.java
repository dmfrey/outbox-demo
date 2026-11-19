package com.example.outboxdemo.order;

import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Closes the loop: the order module learns the outcome of the stock reservation. */
@Component
class StockResultListener {

    private final OrderRepository orders;

    StockResultListener( OrderRepository orders ) {
        this.orders = orders;
    }

    // @ApplicationModuleListener = async + new transaction + runs AFTER the publisher committed.
    // The registry marks the publication complete when the method returns normally; if it throws,
    // the publication stays incomplete and is re-delivered later.
    @ApplicationModuleListener
    void on( StockReserved event ) {
        orders.updateStatus( event.orderId(), OrderStatus.CONFIRMED );
    }

    @ApplicationModuleListener
    void on( StockRejected event ) {
        orders.updateStatus( event.orderId(), OrderStatus.REJECTED );
    }

}
