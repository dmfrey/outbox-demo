package com.example.outboxdemo.notificationservice;

import com.example.outboxdemo.events.OrderPlaced;
import com.example.outboxdemo.events.StockReserved;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * The extracted service, on its own: no monolith, no database. We play the part of the monolith by
 * publishing the same JSON the monolith's @Externalized events produce, and check the emails.
 */
@SpringBootTest
@Import( TestcontainersConfiguration.class )
class NotificationServiceTest {

    @Autowired RabbitTemplate rabbitTemplate;
    @Autowired JsonMapper jsonMapper;
    @Autowired GenericContainer<?> mailpit;

    @Test
    void eventsFromTheBrokerBecomeEmails() {
        var orderId = UUID.randomUUID();

        publish( OrderPlaced.ROUTING_KEY, new OrderPlaced( orderId, "erin@example.com", List.of( new OrderPlaced.Line( "BOOK-1", 1 ) ) ) );
        publish( StockReserved.ROUTING_KEY, new StockReserved( orderId, "erin@example.com" ) );

        await().atMost( Duration.ofSeconds( 20 ) ).untilAsserted( () ->
                assertThat( subjectsFor( "erin@example.com" ) )
                        .anyMatch( s -> s.equals( "We received your order " + orderId ) )
                        .anyMatch( s -> s.equals( "Your order " + orderId + " is confirmed" ) ) );
    }

    private void publish( String routingKey, Object event ) {
        rabbitTemplate.send( "domain.events", routingKey, MessageBuilder
                .withBody( jsonMapper.writeValueAsBytes( event ) )
                .setContentType( MessageProperties.CONTENT_TYPE_JSON ).build() );
    }

    private List<String> subjectsFor( String recipient ) {
        var client = RestClient.create( "http://" + mailpit.getHost() + ":" + mailpit.getMappedPort( 8025 ) );
        var body = client.get().uri( "/api/v1/messages" ).retrieve().body( String.class );
        var subjects = new ArrayList<String>();
        for ( JsonNode m : jsonMapper.readTree( body ).path( "messages" ) ) {
            for ( JsonNode to : m.path( "To" ) ) {
                if ( recipient.equals( to.path( "Address" ).asString() ) ) {
                    subjects.add( m.path( "Subject" ).asString() );
                }
            }
        }
        return subjects;
    }

}
