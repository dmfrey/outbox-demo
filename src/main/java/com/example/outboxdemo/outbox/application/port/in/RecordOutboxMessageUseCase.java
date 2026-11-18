package com.example.outboxdemo.outbox.application.port.in;

/**
 * Called by other modules, from inside their own business transaction, to say "this event must
 * eventually be published". Deliberately does NOT publish anything itself.
 */
public interface RecordOutboxMessageUseCase {

    void execute( RecordOutboxMessageCommand command );

    record RecordOutboxMessageCommand( String aggregateType, String aggregateId, String eventType, Object payload ) {
    }

}
