package com.example.outboxdemo.outbox.inbox;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * The consumer-side half of the pattern. The relay guarantees at-least-once delivery, so a
 * message can arrive twice. {@code runOnce} records the message id and runs the work in ONE
 * transaction: either both happen or neither does. A redelivery hits the primary key and is
 * silently skipped.
 *
 * <p>This lives in the messaging adapters (not in the domain services) because de-duplication is
 * a property of the transport, not of the business rule.
 */
@Component
public class InboxGuard {

    private final JdbcClient jdbc;

    InboxGuard( JdbcClient jdbc ) {
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean runOnce( String consumer, UUID messageId, Runnable work ) {
        var inserted = jdbc.sql( """
                INSERT INTO processed_message (consumer, message_id) VALUES (:consumer, :messageId)
                ON CONFLICT DO NOTHING
                """ )
                .param( "consumer", consumer )
                .param( "messageId", messageId )
                .update();

        if ( inserted == 0 ) {
            return false; // duplicate delivery: already handled
        }
        work.run();
        return true;
    }

}
