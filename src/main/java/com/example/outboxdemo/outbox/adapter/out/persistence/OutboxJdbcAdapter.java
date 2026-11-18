package com.example.outboxdemo.outbox.adapter.out.persistence;

import com.example.outboxdemo.outbox.application.domain.model.OutboxMessage;
import com.example.outboxdemo.outbox.application.port.out.LoadPendingOutboxMessagesPort;
import com.example.outboxdemo.outbox.application.port.out.MarkOutboxMessagesPublishedPort;
import com.example.outboxdemo.outbox.application.port.out.SaveOutboxMessagePort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
class OutboxJdbcAdapter implements SaveOutboxMessagePort, LoadPendingOutboxMessagesPort, MarkOutboxMessagesPublishedPort {

    private final JdbcClient jdbc;

    OutboxJdbcAdapter( JdbcClient jdbc ) {
        this.jdbc = jdbc;
    }

    @Override
    public void save( OutboxMessage m ) {
        jdbc.sql( """
                INSERT INTO outbox_message (id, aggregate_type, aggregate_id, event_type, payload)
                VALUES (:id, :aggregateType, :aggregateId, :eventType, CAST(:payload AS jsonb))
                """ )
                .param( "id", m.id() )
                .param( "aggregateType", m.aggregateType() )
                .param( "aggregateId", m.aggregateId() )
                .param( "eventType", m.eventType() )
                .param( "payload", m.payload() )
                .update();
    }

    @Override
    public List<OutboxMessage> loadPending( int limit ) {
        // SKIP LOCKED: rows another relay instance has locked are skipped rather than waited on.
        return jdbc.sql( """
                SELECT id, aggregate_type, aggregate_id, event_type, payload::text AS payload
                FROM outbox_message
                WHERE published_at IS NULL
                ORDER BY created_at, id
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
                """ )
                .param( "limit", limit )
                .query( ( rs, i ) -> new OutboxMessage(
                        rs.getObject( "id", UUID.class ),
                        rs.getString( "aggregate_type" ),
                        rs.getString( "aggregate_id" ),
                        rs.getString( "event_type" ),
                        rs.getString( "payload" ) ) )
                .list();
    }

    @Override
    public void markPublished( Collection<UUID> ids ) {
        jdbc.sql( "UPDATE outbox_message SET published_at = now() WHERE id IN (:ids)" )
                .param( "ids", ids )
                .update();
    }

}
