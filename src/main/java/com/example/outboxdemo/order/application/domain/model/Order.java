package com.example.outboxdemo.order.application.domain.model;

import java.util.List;
import java.util.UUID;

public record Order( UUID id, String customerEmail, OrderStatus status, List<OrderLine> lines ) {

    public static Order place( String customerEmail, List<OrderLine> lines ) {
        return new Order( UUID.randomUUID(), customerEmail, OrderStatus.PLACED, List.copyOf( lines ) );
    }

}
