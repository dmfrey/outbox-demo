package com.example.outboxdemo.notificationservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

/** Just RabbitMQ and Mailpit. No Postgres: this service has no database. */
@TestConfiguration( proxyBeanMethods = false )
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    RabbitMQContainer rabbit() {
        return new RabbitMQContainer( DockerImageName.parse( "rabbitmq:4-management" ) );
    }

    @Bean
    GenericContainer<?> mailpit() {
        return new GenericContainer<>( DockerImageName.parse( "axllent/mailpit:v1.21" ) ).withExposedPorts( 1025, 8025 );
    }

    @Bean
    DynamicPropertyRegistrar mailProperties( GenericContainer<?> mailpit ) {
        return registry -> {
            registry.add( "spring.mail.host", mailpit::getHost );
            registry.add( "spring.mail.port", () -> mailpit.getMappedPort( 1025 ) );
        };
    }

}
