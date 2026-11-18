package com.example.outboxdemo.inventory.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ReserveStockUseCase {

    void execute( ReserveStockCommand command );

    record ReserveStockCommand( UUID orderId, String customerEmail, List<Line> lines ) {
    }

    record Line( String sku, int quantity ) {
    }

}
