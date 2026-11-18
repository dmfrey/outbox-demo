package com.example.outboxdemo.notification.application.port.in;

import java.util.UUID;

public interface SendOrderEmailUseCase {

    void execute( SendOrderEmailCommand command );

    enum Kind { RECEIVED, CONFIRMED, BACKORDERED }

    record SendOrderEmailCommand( UUID orderId, String customerEmail, Kind kind ) {
    }

}
