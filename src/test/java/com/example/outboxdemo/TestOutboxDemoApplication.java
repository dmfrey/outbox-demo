package com.example.outboxdemo;

import org.springframework.boot.SpringApplication;

/** Run with {@code ./gradlew bootTestRun}: starts the app against throwaway containers (Mailpit UI is mapped to a random port - see the logs). */
public class TestOutboxDemoApplication {

    public static void main( String[] args ) {
        SpringApplication.from( OutboxDemoApplication::main ).with( TestcontainersConfiguration.class ).run( args );
    }

}
