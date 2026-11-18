package com.example.outboxdemo.order;

import java.util.List;
import java.util.UUID;

public record Order( UUID id, String customerEmail, OrderStatus status, List<OrderLine> lines ) {
}
