package com.example.outboxdemo;

import com.example.outboxdemo.outbox.configuration.OutboxMessagingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/** LESSON 3: at-least-once delivery means duplicates happen; the inbox makes them harmless. */
class DuplicateDeliveryTest extends AbstractIntegrationTest {

    @Autowired RabbitTemplate rabbitTemplate;

    @Test
    void sameMessageDeliveredTwiceSendsOnlyOneEmail() {
        var messageId = UUID.randomUUID().toString();
        var payload = """
                {"orderId":"%s","customerEmail":"carol@example.com","lines":[]}""".formatted( UUID.randomUUID() );

        // Simulate the relay crashing after the broker confirmed but before it marked the row published:
        // the very same outbox message is sent a second time.
        for ( int i = 0; i < 2; i++ ) {
            rabbitTemplate.send( OutboxMessagingConfiguration.EVENTS_EXCHANGE, "order.placed", MessageBuilder
                    .withBody( payload.getBytes() ).setContentType( MessageProperties.CONTENT_TYPE_JSON )
                    .setMessageId( messageId ).setType( "order.placed" ).build() );
        }

        await().atMost( Duration.ofSeconds( 20 ) ).untilAsserted( () ->
                assertThat( receivedEmails() ).isEqualTo( 1L ) );
        // Give the duplicate time to be (not) processed, then make sure it still is exactly one.
        await().during( Duration.ofSeconds( 2 ) ).atMost( Duration.ofSeconds( 5 ) ).untilAsserted( () ->
                assertThat( receivedEmails() ).isEqualTo( 1L ) );
        assertThat( jdbc.sql( "SELECT count(*) FROM processed_message WHERE message_id = :id::uuid AND consumer = 'notification.order-events'" )
                .param( "id", messageId ).query( Integer.class ).single() ).isEqualTo( 1 );
    }

    // Inventory also consumes order.placed (and, with no lines to reserve, later triggers a "confirmed"
    // email), so count only the "received" emails - the ones produced by the duplicated message.
    private long receivedEmails() {
        return emailSubjectsFor( "carol@example.com" ).stream().filter( s -> s.startsWith( "We received your order" ) ).count();
    }

}
