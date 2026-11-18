package com.example.outboxdemo.notification.application.domain.service;

import com.example.outboxdemo.notification.application.port.in.SendOrderEmailUseCase;
import com.example.outboxdemo.notification.application.port.out.SendEmailPort;
import org.springframework.stereotype.Service;

@Service
class SendOrderEmailService implements SendOrderEmailUseCase {

    private final SendEmailPort sendEmailPort;

    SendOrderEmailService( SendEmailPort sendEmailPort ) {
        this.sendEmailPort = sendEmailPort;
    }

    @Override
    public void execute( SendOrderEmailCommand command ) {
        var subject = switch ( command.kind() ) {
            case RECEIVED -> "We received your order " + command.orderId();
            case CONFIRMED -> "Your order " + command.orderId() + " is confirmed";
            case BACKORDERED -> "Your order " + command.orderId() + " is backordered";
        };
        sendEmailPort.send( command.customerEmail(), subject, subject + "\n\nThanks for shopping with us!" );
    }

}
