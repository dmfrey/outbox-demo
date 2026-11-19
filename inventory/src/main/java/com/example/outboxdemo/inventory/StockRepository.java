package com.example.outboxdemo.inventory;

import com.example.outboxdemo.inventory.ReservationRequest.Item;
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

    /** All-or-nothing: reserves every item, or none of them. Returns whether it succeeded. */
    boolean reserve( List<Item> items ) {
        // Lock rows in a stable order (avoids deadlocks), check every item, then decrement.
        var sorted = items.stream().sorted( Comparator.comparing( Item::sku ) ).toList();
        for ( var item : sorted ) {
            var available = jdbc.sql( "SELECT available FROM stock WHERE sku = :sku FOR UPDATE" )
                    .param( "sku", item.sku() )
                    .query( Integer.class )
                    .optional();
            if ( available.isEmpty() || available.get() < item.quantity() ) {
                return false;
            }
        }
        for ( var item : sorted ) {
            jdbc.sql( "UPDATE stock SET available = available - :qty WHERE sku = :sku" )
                    .param( "qty", item.quantity() )
                    .param( "sku", item.sku() )
                    .update();
        }
        return true;
    }

}
