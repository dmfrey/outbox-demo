package com.example.outboxdemo.outbox.application.domain.service;

import com.example.outboxdemo.outbox.application.port.out.LoadPendingOutboxMessagesPort;
import com.example.outboxdemo.outbox.application.port.out.MarkOutboxMessagesPublishedPort;
import com.example.outboxdemo.outbox.application.port.out.PublishOutboxMessagesPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

/**
 * The relay: moves rows from the outbox table to the broker.
 *
 * <p>Delivery guarantee is <b>at-least-once</b>. If the process dies after the broker confirmed a
 * batch but before the transaction below commits, those rows are still unpublished and will be
 * sent again. That is why every consumer must be idempotent (see {@code InboxGuard}).
 *
 * <p>{@code FOR UPDATE SKIP LOCKED} (in the persistence adapter) lets several relay instances run
 * side by side without double-sending: each grabs a different set of rows.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger( OutboxRelay.class );

    private final LoadPendingOutboxMessagesPort loadPendingOutboxMessagesPort;
    private final PublishOutboxMessagesPort publishOutboxMessagesPort;
    private final MarkOutboxMessagesPublishedPort markOutboxMessagesPublishedPort;
    private final TransactionTemplate transactionTemplate;
    private final int batchSize;

    OutboxRelay(
            LoadPendingOutboxMessagesPort loadPendingOutboxMessagesPort,
            PublishOutboxMessagesPort publishOutboxMessagesPort,
            MarkOutboxMessagesPublishedPort markOutboxMessagesPublishedPort,
            TransactionTemplate transactionTemplate,
            @Value( "${outbox.relay.batch-size:50}" ) int batchSize
    ) {
        this.loadPendingOutboxMessagesPort = loadPendingOutboxMessagesPort;
        this.publishOutboxMessagesPort = publishOutboxMessagesPort;
        this.markOutboxMessagesPublishedPort = markOutboxMessagesPublishedPort;
        this.transactionTemplate = transactionTemplate;
        this.batchSize = batchSize;
    }

    @Scheduled( fixedDelayString = "${outbox.relay.poll-interval:1s}" )
    public void relayPendingMessages() {
        try {
            transactionTemplate.executeWithoutResult( status -> {
                var pending = loadPendingOutboxMessagesPort.loadPending( batchSize );
                if ( pending.isEmpty() ) {
                    return;
                }

                // Throws if the broker does not confirm -> transaction rolls back -> rows stay
                // unpublished -> next poll retries. Nothing is lost, nothing is marked early.
                publishOutboxMessagesPort.publish( pending );
                markOutboxMessagesPublishedPort.markPublished( pending.stream().map( m -> m.id() ).toList() );
                log.debug( "Relayed {} outbox message(s)", pending.size() );
            } );
        } catch ( RuntimeException e ) {
            // Broker down, network blip, etc. Log and try again on the next tick.
            log.warn( "Outbox relay failed, will retry: {}", e.getMessage() );
        }
    }

}
