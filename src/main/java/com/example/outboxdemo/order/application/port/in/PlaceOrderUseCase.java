package com.example.outboxdemo.order.application.port.in;

import com.example.outboxdemo.order.application.domain.model.Order;
import com.example.outboxdemo.order.application.domain.model.OrderLine;

import java.util.List;

public interface PlaceOrderUseCase {

    Order execute( PlaceOrderCommand command );

    record PlaceOrderCommand( String customerEmail, List<OrderLine> lines ) {
    }

}
