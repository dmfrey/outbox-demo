package com.example.outboxdemo.outbox.application.domain.service;

import com.example.outboxdemo.outbox.application.domain.model.OutboxMessage;
import com.example.outboxdemo.outbox.application.port.in.RecordOutboxMessageUseCase;
import com.example.outboxdemo.outbox.application.port.out.SaveOutboxMessagePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
class RecordOutboxMessageService implements RecordOutboxMessageUseCase {

    private final SaveOutboxMessagePort saveOutboxMessagePort;
    private final JsonMapper jsonMapper;

    RecordOutboxMessageService( SaveOutboxMessagePort saveOutboxMessagePort, JsonMapper jsonMapper ) {
        this.saveOutboxMessagePort = saveOutboxMessagePort;
        this.jsonMapper = jsonMapper;
    }

    // THE KEY LINE OF THE WHOLE PATTERN. MANDATORY means: "there must already be a transaction
    // running - join it, and fail loudly if there isn't." The outbox row therefore commits or
    // rolls back together with the business change. Call this with no transaction and it throws
    // instead of quietly reintroducing the dual-write problem.
    @Override
    @Transactional( propagation = Propagation.MANDATORY )
    public void execute( RecordOutboxMessageCommand command ) {
        saveOutboxMessagePort.save( new OutboxMessage(
                UUID.randomUUID(),
                command.aggregateType(),
                command.aggregateId(),
                command.eventType(),
                jsonMapper.writeValueAsString( command.payload() )
        ) );
    }

}
