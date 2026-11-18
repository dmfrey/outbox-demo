package com.example.outboxdemo.order.application.domain.service;

import com.example.outboxdemo.order.application.port.in.UpdateOrderStatusUseCase;
import com.example.outboxdemo.order.application.port.out.UpdateOrderStatusPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateOrderStatusService implements UpdateOrderStatusUseCase {

    private final UpdateOrderStatusPort updateOrderStatusPort;

    UpdateOrderStatusService( UpdateOrderStatusPort updateOrderStatusPort ) {
        this.updateOrderStatusPort = updateOrderStatusPort;
    }

    @Override
    @Transactional
    public void execute( UpdateOrderStatusCommand command ) {
        updateOrderStatusPort.updateStatus( command.orderId(), command.status() );
    }

}
