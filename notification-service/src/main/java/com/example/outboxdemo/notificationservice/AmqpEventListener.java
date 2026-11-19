package com.example.outboxdemo.notificationservice;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;
import com.example.outboxdemo.notification.NotificationService;
import com.example.outboxdemo.notification.OrderNotificationMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The RabbitMQ adapter: the only new code needed to run the notification module as a service.
 * Everything behind it - the mapper, NotificationService - is the same code the monolith uses.
 *
 * <p>Delivery is at-least-once, and this service has no database to de-duplicate in, so a redelivered
 * message sends a second email. To prevent that, add an inbox table (see the manual-outbox branch,
 * {@code InboxGuard}) or make the downstream action naturally idempotent.
 */
@Component
class AmqpEventListener {

    static final String QUEUE = "notification-service.events";

    private final NotificationService notifications;
    private final JsonMapper jsonMapper;

    AmqpEventListener( NotificationService notifications, JsonMapper jsonMapper ) {
        this.notifications = notifications;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener( queues = QUEUE )
    void on( Message message ) {
        var body = message.getBody();
        var notification = switch ( message.getMessageProperties().getReceivedRoutingKey() ) {
            case OrderPlaced.ROUTING_KEY -> OrderNotificationMapper.from( jsonMapper.readValue( body, OrderPlaced.class ) );
            case StockReserved.ROUTING_KEY -> OrderNotificationMapper.from( jsonMapper.readValue( body, StockReserved.class ) );
            case StockRejected.ROUTING_KEY -> OrderNotificationMapper.from( jsonMapper.readValue( body, StockRejected.class ) );
            default -> throw new IllegalArgumentException( "Unexpected routing key " + message.getMessageProperties().getReceivedRoutingKey() );
        };
        notifications.send( notification );
    }

}
