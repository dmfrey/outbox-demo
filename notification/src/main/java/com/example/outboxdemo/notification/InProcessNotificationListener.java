package com.example.outboxdemo.notification;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * The in-process adapter, used while notification lives inside the monolith.
 *
 * <p>Switch it off ({@code notification.in-process.enabled=false}) once the module has been
 * extracted into notification-service, which receives the same events over RabbitMQ instead.
 * Without the switch both would send an email.
 */
@Component
@ConditionalOnProperty( prefix = "notification.in-process", name = "enabled", havingValue = "true", matchIfMissing = true )
class InProcessNotificationListener {

    private final NotificationService notifications;

    InProcessNotificationListener( NotificationService notifications ) {
        this.notifications = notifications;
    }

    @ApplicationModuleListener
    void on( OrderPlaced event ) {
        notifications.send( OrderNotificationMapper.from( event ) );
    }

    @ApplicationModuleListener
    void on( StockReserved event ) {
        notifications.send( OrderNotificationMapper.from( event ) );
    }

    @ApplicationModuleListener
    void on( StockRejected event ) {
        notifications.send( OrderNotificationMapper.from( event ) );
    }

}
