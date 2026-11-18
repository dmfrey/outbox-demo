package com.example.outboxdemo.inventory.application.port.out;

import java.util.UUID;

public interface RecordStockReservedPort {

    void record( UUID orderId, String customerEmail );

}
