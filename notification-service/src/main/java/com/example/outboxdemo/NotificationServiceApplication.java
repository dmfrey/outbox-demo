package com.example.outboxdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The notification module, deployed on its own. Component scanning starts at the same root package
 * as the monolith, but only finds what is on THIS classpath: the notification module and the adapter
 * in the notificationservice package - not order or inventory.
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main( String[] args ) {
        SpringApplication.run( NotificationServiceApplication.class, args );
    }

}
