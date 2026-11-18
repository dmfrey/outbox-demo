package com.example.outboxdemo.order;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class OrderRepository {

    private final JdbcClient jdbc;

    OrderRepository( JdbcClient jdbc ) {
        this.jdbc = jdbc;
    }

    void save( Order order ) {
        jdbc.sql( "INSERT INTO orders (id, customer_email, status) VALUES (:id, :email, :status)" )
                .param( "id", order.id() )
                .param( "email", order.customerEmail() )
                .param( "status", order.status().name() )
                .update();
        for ( var line : order.lines() ) {
            jdbc.sql( "INSERT INTO order_line (order_id, sku, quantity) VALUES (:orderId, :sku, :quantity)" )
                    .param( "orderId", order.id() )
                    .param( "sku", line.sku() )
                    .param( "quantity", line.quantity() )
                    .update();
        }
    }

    Optional<Order> find( UUID id ) {
        var lines = jdbc.sql( "SELECT sku, quantity FROM order_line WHERE order_id = :id ORDER BY sku" )
                .param( "id", id )
                .query( ( rs, i ) -> new OrderLine( rs.getString( "sku" ), rs.getInt( "quantity" ) ) )
                .list();
        return jdbc.sql( "SELECT id, customer_email, status FROM orders WHERE id = :id" )
                .param( "id", id )
                .query( ( rs, i ) -> new Order(
                        rs.getObject( "id", UUID.class ),
                        rs.getString( "customer_email" ),
                        OrderStatus.valueOf( rs.getString( "status" ) ),
                        lines ) )
                .optional();
    }

    void updateStatus( UUID id, OrderStatus status ) {
        jdbc.sql( "UPDATE orders SET status = :status WHERE id = :id" )
                .param( "status", status.name() )
                .param( "id", id )
                .update();
    }

}
