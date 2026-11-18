package com.example.outboxdemo.order;

import com.example.outboxdemo.events.OrderPlaced;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderManagement {

    private final OrderRepository orders;
    private final ApplicationEventPublisher events;

    OrderManagement( OrderRepository orders, ApplicationEventPublisher events ) {
        this.orders = orders;
        this.events = events;
    }

    // Compare with the manual-outbox branch: there, this method called an outbox use case, which
    // wrote a row by hand. Here we just publish a normal Spring event. Because it is published inside
    // this @Transactional method, Modulith writes it to the event publication registry in the SAME
    // transaction as the order row - same guarantee, no outbox code of our own.
    @Transactional
    public Order place( String customerEmail, List<OrderLine> lines ) {
        var order = new Order( UUID.randomUUID(), customerEmail, OrderStatus.PLACED, List.copyOf( lines ) );
        orders.save( order );
        events.publishEvent( new OrderPlaced( order.id(), customerEmail,
                lines.stream().map( l -> new OrderPlaced.Line( l.sku(), l.quantity() ) ).toList() ) );
        return order;
    }

    public Optional<Order> find( UUID id ) {
        return orders.find( id );
    }

}
