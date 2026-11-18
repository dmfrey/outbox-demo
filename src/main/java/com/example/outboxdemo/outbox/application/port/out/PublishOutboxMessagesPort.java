package com.example.outboxdemo.outbox.application.port.out;

import com.example.outboxdemo.outbox.application.domain.model.OutboxMessage;

import java.util.List;

public interface PublishOutboxMessagesPort {

    /** Must not return until the broker has confirmed every message; throws otherwise. */
    void publish( List<OutboxMessage> messages );

}
