package com.example.outboxdemo.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** The module's core: turns an {@link OrderNotification} into an email. Transport-agnostic. */
@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    NotificationService( JavaMailSender mailSender ) {
        this.mailSender = mailSender;
    }

    public void send( OrderNotification notification ) {
        var subject = switch ( notification.kind() ) {
            case RECEIVED -> "We received your order " + notification.orderId();
            case CONFIRMED -> "Your order " + notification.orderId() + " is confirmed";
            case BACKORDERED -> "Your order " + notification.orderId() + " is backordered";
        };

        var message = new SimpleMailMessage();
        message.setFrom( "orders@example.com" );
        message.setTo( notification.recipient() );
        message.setSubject( subject );
        message.setText( subject + "\n\nThanks for shopping with us!" );
        mailSender.send( message );
    }

}
