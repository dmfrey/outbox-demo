package com.example.outboxdemo;

import com.example.outboxdemo.order.application.domain.model.OrderLine;
import com.example.outboxdemo.order.application.port.in.PlaceOrderUseCase;
import com.example.outboxdemo.order.application.port.in.PlaceOrderUseCase.PlaceOrderCommand;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase.RecordOutboxMessageCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** LESSON 1: the order row and the outbox row commit - or roll back - together. */
class OutboxAtomicityTest extends AbstractIntegrationTest {

    @Autowired PlaceOrderUseCase placeOrderUseCase;
    @Autowired RecordOutboxMessageUseCase recordOutboxMessageUseCase;
    @Autowired TransactionTemplate tx;

    private final PlaceOrderCommand command = new PlaceOrderCommand( "a@example.com", List.of( new OrderLine( "BOOK-1", 1 ) ) );

    @Test
    void orderAndOutboxMessageAreWrittenTogether() {
        var order = placeOrderUseCase.execute( command );

        assertThat( count( "orders" ) ).isEqualTo( 1 );
        var rows = jdbc.sql( "SELECT event_type, aggregate_id FROM outbox_message" ).query().listOfRows();
        assertThat( rows ).hasSize( 1 );
        assertThat( rows.getFirst() ).containsEntry( "event_type", "order.placed" )
                .containsEntry( "aggregate_id", order.id().toString() );
    }

    @Test
    void ifTheTransactionRollsBackNeitherRowSurvives() {
        assertThatThrownBy( () -> tx.executeWithoutResult( status -> {
            placeOrderUseCase.execute( command );   // joins this transaction
            throw new IllegalStateException( "boom after the order was saved" );
        } ) ).isInstanceOf( IllegalStateException.class );

        assertThat( count( "orders" ) ).isZero();
        assertThat( count( "outbox_message" ) ).isZero();   // no phantom event for an order that never existed
    }

    @Test
    void recordingAnEventOutsideATransactionIsRefused() {
        assertThatThrownBy( () -> recordOutboxMessageUseCase.execute(
                new RecordOutboxMessageCommand( "Order", "1", "order.placed", Map.of() ) ) )
                .isInstanceOf( IllegalTransactionStateException.class );
    }

    private int count( String table ) {
        return jdbc.sql( "SELECT count(*) FROM " + table ).query( Integer.class ).single();
    }

}
