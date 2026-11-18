package com.example.outboxdemo.order.application.port.out;

import com.example.outboxdemo.order.application.domain.model.Order;

import java.util.Optional;
import java.util.UUID;

public interface LoadOrderPort {

    Optional<Order> load( UUID orderId );

}
