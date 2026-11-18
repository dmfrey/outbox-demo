package com.example.outboxdemo.outbox.application.port.out;

import java.util.Collection;
import java.util.UUID;

public interface MarkOutboxMessagesPublishedPort {

    void markPublished( Collection<UUID> ids );

}
