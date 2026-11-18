package com.example.outboxdemo.inventory.application.port.out;

import com.example.outboxdemo.inventory.application.port.in.ReserveStockUseCase.Line;

import java.util.List;

public interface ReserveStockPort {

    /** All-or-nothing: reserves every line, or none of them. Returns whether it succeeded. */
    boolean reserve( List<Line> lines );

}
