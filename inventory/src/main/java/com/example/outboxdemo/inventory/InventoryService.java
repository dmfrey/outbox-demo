package com.example.outboxdemo.inventory;

import org.springframework.stereotype.Service;

@Service
class InventoryService {

    enum Outcome { RESERVED, INSUFFICIENT_STOCK }

    private final StockRepository stock;

    InventoryService( StockRepository stock ) {
        this.stock = stock;
    }

    Outcome reserve( ReservationRequest request ) {
        return stock.reserve( request.items() ) ? Outcome.RESERVED : Outcome.INSUFFICIENT_STOCK;
    }

}
