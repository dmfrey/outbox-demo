package com.example.outboxdemo.notification;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;

import static com.example.outboxdemo.notification.OrderNotification.Kind;

/**
 * Translates the shared event contracts into this module's own type. Public because every
 * transport adapter - the in-process listener here, the RabbitMQ listener in notification-service -
 * needs the same translation.
 */
public final class OrderNotificationMapper {

    private OrderNotificationMapper() {
    }

    public static OrderNotification from( OrderPlaced event ) {
        return new OrderNotification( event.orderId(), event.customerEmail(), Kind.RECEIVED );
    }

    public static OrderNotification from( StockReserved event ) {
        return new OrderNotification( event.orderId(), event.customerEmail(), Kind.CONFIRMED );
    }

    public static OrderNotification from( StockRejected event ) {
        return new OrderNotification( event.orderId(), event.customerEmail(), Kind.BACKORDERED );
    }

}
