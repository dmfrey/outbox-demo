package com.example.outboxdemo.notification.application.port.out;

public interface SendEmailPort {

    void send( String to, String subject, String body );

}
