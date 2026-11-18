package com.example.outboxdemo.order.application.port.out;

import com.example.outboxdemo.order.application.domain.model.Order;

public interface SaveOrderPort {

    void save( Order order );

}
