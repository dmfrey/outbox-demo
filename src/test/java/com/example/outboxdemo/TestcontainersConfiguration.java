package com.example.outboxdemo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

/** Everything the app needs, as throwaway containers: Postgres, RabbitMQ and Mailpit (a fake SMTP server with a web UI/API). */
@TestConfiguration( proxyBeanMethods = false )
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer( DockerImageName.parse( "postgres:17" ) );
    }

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
