package com.example.outboxdemo.outbox.application.port.out;

import com.example.outboxdemo.outbox.application.domain.model.OutboxMessage;

public interface SaveOutboxMessagePort {

    void save( OutboxMessage message );

}
