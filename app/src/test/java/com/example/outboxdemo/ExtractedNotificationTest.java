package com.example.outboxdemo;

import com.example.outboxdemo.order.OrderLine;
import com.example.outboxdemo.order.OrderManagement;
import com.example.outboxdemo.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * LESSON 3 (monolith side): when notification is handed to notification-service the monolith just
 * stops sending emails itself. Ordering and stock reservation are unaffected, and the events still
 * leave for RabbitMQ (see ExternalizationTest), where the extracted service picks them up.
 */
@TestPropertySource( properties = "notification.in-process.enabled=false" )
class ExtractedNotificationTest extends AbstractIntegrationTest {

    @Autowired OrderManagement orders;

    @Test
    void ordersStillFlowButTheMonolithSendsNoEmail() {
        var order = orders.place( "frank@example.com", List.of( new OrderLine( "BOOK-1", 1 ) ) );

        await().atMost( Duration.ofSeconds( 20 ) ).untilAsserted( () ->
                assertThat( orders.find( order.id() ).orElseThrow().status() ).isEqualTo( OrderStatus.CONFIRMED ) );

        await().during( Duration.ofSeconds( 3 ) ).atMost( Duration.ofSeconds( 8 ) ).untilAsserted( () ->
                assertThat( emailSubjectsFor( "frank@example.com" ) ).isEmpty() );
    }

}
