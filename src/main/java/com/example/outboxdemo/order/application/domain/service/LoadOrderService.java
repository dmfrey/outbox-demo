package com.example.outboxdemo.order.application.domain.service;

import com.example.outboxdemo.order.application.domain.model.Order;
import com.example.outboxdemo.order.application.port.in.LoadOrderUseCase;
import com.example.outboxdemo.order.application.port.out.LoadOrderPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class LoadOrderService implements LoadOrderUseCase {

    private final LoadOrderPort loadOrderPort;

    LoadOrderService( LoadOrderPort loadOrderPort ) {
        this.loadOrderPort = loadOrderPort;
    }

    @Override
    public Optional<Order> execute( LoadOrderCommand command ) {
        return loadOrderPort.load( command.orderId() );
    }

}
