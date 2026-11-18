package com.example.outboxdemo.inventory;

import com.example.outboxdemo.events.OrderPlaced;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

@Repository
class StockRepository {

    private final JdbcClient jdbc;

    StockRepository( JdbcClient jdbc ) {
        this.jdbc = jdbc;
    }

    /** All-or-nothing: reserves every line, or none of them. Returns whether it succeeded. */
    boolean reserve( List<OrderPlaced.Line> lines ) {
        // Lock rows in a stable order (avoids deadlocks), check every line, then decrement.
        var sorted = lines.stream().sorted( Comparator.comparing( OrderPlaced.Line::sku ) ).toList();
        for ( var line : sorted ) {
            var available = jdbc.sql( "SELECT available FROM stock WHERE sku = :sku FOR UPDATE" )
                    .param( "sku", line.sku() )
                    .query( Integer.class )
                    .optional();
            if ( available.isEmpty() || available.get() < line.quantity() ) {
                return false;
            }
        }
        for ( var line : sorted ) {
            jdbc.sql( "UPDATE stock SET available = available - :qty WHERE sku = :sku" )
                    .param( "qty", line.quantity() )
                    .param( "sku", line.sku() )
                    .update();
        }
        return true;
    }

}
