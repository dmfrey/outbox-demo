package com.example.outboxdemo.order.application.port.out;

import com.example.outboxdemo.order.application.domain.model.OrderPlaced;

/**
 * "Make sure the world eventually hears about this." The domain does not know (or care) that the
 * adapter behind this port writes to an outbox table rather than talking to a broker directly.
 */
public interface RecordOrderPlacedPort {

    void record( OrderPlaced event );

}
