package com.example.outboxdemo;

import com.example.outboxdemo.order.OrderLine;
import com.example.outboxdemo.order.OrderManagement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/** LESSON 1: Modulith's event publication registry gives the same guarantee as our hand-rolled outbox. */
class EventPublicationTest extends AbstractIntegrationTest {

    @Autowired OrderManagement orders;
    @Autowired TransactionTemplate tx;

    private final List<OrderLine> lines = List.of( new OrderLine( "BOOK-1", 1 ) );

    @Test
    void eventIsStoredWithTheOrderAndCompletedAfterTheListenersRan() {
        orders.place( "a@example.com", lines );

        assertThat( count( "orders" ) ).isEqualTo( 1 );
        // The registry row was written in the order's transaction (one row per listener of OrderPlaced)...
        assertThat( count( "event_publication" ) ).isGreaterThanOrEqualTo( 1 );
        // ...and each is marked complete once its listener finished.
        await().atMost( Duration.ofSeconds( 20 ) ).untilAsserted( () ->
                assertThat( jdbc.sql( "SELECT count(*) FROM event_publication WHERE completion_date IS NULL" )
                        .query( Integer.class ).single() ).isZero() );
    }

    @Test
    void ifTheTransactionRollsBackNeitherTheOrderNorTheEventSurvives() {
        assertThatThrownBy( () -> tx.executeWithoutResult( status -> {
            orders.place( "a@example.com", lines );   // joins this transaction
            throw new IllegalStateException( "boom after the order was saved" );
        } ) ).isInstanceOf( IllegalStateException.class );

        assertThat( count( "orders" ) ).isZero();
        assertThat( count( "event_publication" ) ).isZero();   // no phantom event for an order that never existed
    }

    private int count( String table ) {
        return jdbc.sql( "SELECT count(*) FROM " + table ).query( Integer.class ).single();
    }

}
