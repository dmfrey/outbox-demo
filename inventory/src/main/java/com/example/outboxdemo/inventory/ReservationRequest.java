package com.example.outboxdemo.inventory;

import java.util.List;
import java.util.UUID;

/**
 * Inventory's own view of an order: what must be reserved, and nothing else. No customer, no email,
 * no prices - inventory does not care. It is mapped from the shared OrderPlaced event at the module's
 * edge, so the event contract can change without rippling through inventory's domain.
 */
record ReservationRequest( UUID orderId, List<Item> items ) {

    record Item( String sku, int quantity ) {
    }

}
