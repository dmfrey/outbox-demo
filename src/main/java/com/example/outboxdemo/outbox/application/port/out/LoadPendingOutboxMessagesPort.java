package com.example.outboxdemo.outbox.application.port.out;

import com.example.outboxdemo.outbox.application.domain.model.OutboxMessage;

import java.util.List;

public interface LoadPendingOutboxMessagesPort {

    /** Oldest-first, locked for the current transaction so concurrent relays never share a row. */
    List<OutboxMessage> loadPending( int limit );

}
