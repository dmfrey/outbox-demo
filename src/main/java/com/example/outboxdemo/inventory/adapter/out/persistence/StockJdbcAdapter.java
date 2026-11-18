package com.example.outboxdemo.inventory.adapter.out.persistence;

import com.example.outboxdemo.inventory.application.port.in.ReserveStockUseCase.Line;
import com.example.outboxdemo.inventory.application.port.out.ReserveStockPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class StockJdbcAdapter implements ReserveStockPort {

    private final JdbcClient jdbc;

    StockJdbcAdapter( JdbcClient jdbc ) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean reserve( List<Line> lines ) {
        // Lock the rows in a stable order (avoids deadlocks between concurrent reservations),
        // check every line, and only then decrement.
        var sorted = lines.stream().sorted( ( a, b ) -> a.sku().compareTo( b.sku() ) ).toList();
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
