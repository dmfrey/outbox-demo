package com.example.outboxdemo.order.adapter.out.persistence;

import com.example.outboxdemo.order.application.domain.model.Order;
import com.example.outboxdemo.order.application.domain.model.OrderLine;
import com.example.outboxdemo.order.application.domain.model.OrderStatus;
import com.example.outboxdemo.order.application.port.out.LoadOrderPort;
import com.example.outboxdemo.order.application.port.out.SaveOrderPort;
import com.example.outboxdemo.order.application.port.out.UpdateOrderStatusPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class OrderJdbcAdapter implements SaveOrderPort, LoadOrderPort, UpdateOrderStatusPort {

    private final JdbcClient jdbc;

    OrderJdbcAdapter( JdbcClient jdbc ) {
        this.jdbc = jdbc;
    }

    @Override
    public void save( Order order ) {
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

    @Override
    public Optional<Order> load( UUID orderId ) {
        var lines = jdbc.sql( "SELECT sku, quantity FROM order_line WHERE order_id = :id ORDER BY sku" )
                .param( "id", orderId )
                .query( ( rs, i ) -> new OrderLine( rs.getString( "sku" ), rs.getInt( "quantity" ) ) )
                .list();
        return jdbc.sql( "SELECT id, customer_email, status FROM orders WHERE id = :id" )
                .param( "id", orderId )
                .query( ( rs, i ) -> new Order(
                        rs.getObject( "id", UUID.class ),
                        rs.getString( "customer_email" ),
                        OrderStatus.valueOf( rs.getString( "status" ) ),
                        lines ) )
                .optional();
    }

    @Override
    public void updateStatus( UUID orderId, OrderStatus status ) {
        jdbc.sql( "UPDATE orders SET status = :status WHERE id = :id" )
                .param( "status", status.name() )
                .param( "id", orderId )
                .update();
    }

}
