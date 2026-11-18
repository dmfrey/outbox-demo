package com.example.outboxdemo.notification.adapter.out.email;

import com.example.outboxdemo.notification.application.port.out.SendEmailPort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class SmtpEmailAdapter implements SendEmailPort {

    private final JavaMailSender mailSender;

    SmtpEmailAdapter( JavaMailSender mailSender ) {
        this.mailSender = mailSender;
    }

    @Override
    public void send( String to, String subject, String body ) {
        var message = new SimpleMailMessage();
        message.setFrom( "orders@example.com" );
        message.setTo( to );
        message.setSubject( subject );
        message.setText( body );
        mailSender.send( message );
    }

}
