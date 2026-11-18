package com.example.outboxdemo.order.application.port.in;

import com.example.outboxdemo.order.application.domain.model.OrderStatus;

import java.util.UUID;

public interface UpdateOrderStatusUseCase {

    void execute( UpdateOrderStatusCommand command );

    record UpdateOrderStatusCommand( UUID orderId, OrderStatus status ) {
    }

}
