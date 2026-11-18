package com.example.outboxdemo.order.application.port.out;

import com.example.outboxdemo.order.application.domain.model.OrderStatus;

import java.util.UUID;

public interface UpdateOrderStatusPort {

    void updateStatus( UUID orderId, OrderStatus status );

}
