package com.example.outboxdemo.order.application.domain.service;

import com.example.outboxdemo.order.application.domain.model.Order;
import com.example.outboxdemo.order.application.domain.model.OrderPlaced;
import com.example.outboxdemo.order.application.port.in.PlaceOrderUseCase;
import com.example.outboxdemo.order.application.port.out.RecordOrderPlacedPort;
import com.example.outboxdemo.order.application.port.out.SaveOrderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PlaceOrderService implements PlaceOrderUseCase {

    private final SaveOrderPort saveOrderPort;
    private final RecordOrderPlacedPort recordOrderPlacedPort;

    PlaceOrderService( SaveOrderPort saveOrderPort, RecordOrderPlacedPort recordOrderPlacedPort ) {
        this.saveOrderPort = saveOrderPort;
        this.recordOrderPlacedPort = recordOrderPlacedPort;
    }

    // ONE transaction, TWO writes: the order row and the outbox row. There is no "send email" or
    // "call inventory" here, and no broker call either - those happen later, driven by the outbox.
    // If anything below throws, neither row is committed; nothing is ever announced for an order
    // that does not exist, and no order exists without its announcement.
    @Override
    @Transactional
    public Order execute( PlaceOrderCommand command ) {
        var order = Order.place( command.customerEmail(), command.lines() );
        saveOrderPort.save( order );
        recordOrderPlacedPort.record( OrderPlaced.from( order ) );
        return order;
    }

}
