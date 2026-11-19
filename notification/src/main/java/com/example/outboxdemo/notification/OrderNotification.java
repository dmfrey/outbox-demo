package com.example.outboxdemo.notification;

import java.util.UUID;

/**
 * Notification's own view of what happened: who to tell, about which order, and what kind of news.
 * It knows nothing about order lines, stock levels or SKUs.
 */
public record OrderNotification( UUID orderId, String recipient, Kind kind ) {

    public enum Kind { RECEIVED, CONFIRMED, BACKORDERED }

}
