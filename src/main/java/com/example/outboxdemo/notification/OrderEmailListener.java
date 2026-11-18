package com.example.outboxdemo.notification;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockRejected;
import com.example.outboxdemo.events.StockReserved;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.stereotype.Component;


/**
 * The "new module that listens to outbox events". It knows nothing about orders or inventory
 * internals - only the event types. If sending throws, the publication stays incomplete in the
 * registry and is re-delivered on restart (or via IncompleteEventPublications).
 */
@Component
class OrderEmailListener {

    private final JavaMailSender mailSender;

    OrderEmailListener( JavaMailSender mailSender ) {
        this.mailSender = mailSender;
    }

    @ApplicationModuleListener
    void on( OrderPlaced event ) {
        send( event.customerEmail(), "We received your order " + event.orderId() );
    }

    @ApplicationModuleListener
    void on( StockReserved event ) {
        send( event.customerEmail(), "Your order " + event.orderId() + " is confirmed" );
    }

    @ApplicationModuleListener
    void on( StockRejected event ) {
        send( event.customerEmail(), "Your order " + event.orderId() + " is backordered" );
    }

    private void send( String to, String subject ) {
        var message = new SimpleMailMessage();
        message.setFrom( "orders@example.com" );
        message.setTo( to );
        message.setSubject( subject );
        message.setText( subject + "\n\nThanks for shopping with us!" );
        mailSender.send( message );
    }

}
