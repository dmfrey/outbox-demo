package com.example.outboxdemo.order.application.port.in;

import com.example.outboxdemo.order.application.domain.model.Order;

import java.util.Optional;
import java.util.UUID;

public interface LoadOrderUseCase {

    Optional<Order> execute( LoadOrderCommand command );

    record LoadOrderCommand( UUID orderId ) {
    }

}
