package com.example.outboxdemo.order.adapter.in.endpoint;

import com.example.outboxdemo.order.application.domain.model.Order;
import com.example.outboxdemo.order.application.domain.model.OrderLine;
import com.example.outboxdemo.order.application.port.in.LoadOrderUseCase;
import com.example.outboxdemo.order.application.port.in.LoadOrderUseCase.LoadOrderCommand;
import com.example.outboxdemo.order.application.port.in.PlaceOrderUseCase;
import com.example.outboxdemo.order.application.port.in.PlaceOrderUseCase.PlaceOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping( "/orders" )
class OrderEndpoint {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final LoadOrderUseCase loadOrderUseCase;

    OrderEndpoint( PlaceOrderUseCase placeOrderUseCase, LoadOrderUseCase loadOrderUseCase ) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.loadOrderUseCase = loadOrderUseCase;
    }

    record PlaceOrderRequest( @NotBlank @Email String customerEmail, @NotEmpty List<@Valid LineRequest> lines ) {
    }

    record LineRequest( @NotBlank String sku, @Min( 1 ) int quantity ) {
    }

    record OrderResponse( UUID id, String customerEmail, String status, List<OrderLine> lines ) {

        static OrderResponse from( Order o ) {
            return new OrderResponse( o.id(), o.customerEmail(), o.status().name(), o.lines() );
        }

    }

    @PostMapping
    ResponseEntity<OrderResponse> place( @Valid @RequestBody PlaceOrderRequest request ) {
        var lines = request.lines().stream().map( l -> new OrderLine( l.sku(), l.quantity() ) ).toList();
        var order = placeOrderUseCase.execute( new PlaceOrderCommand( request.customerEmail(), lines ) );
        // 202: the order is accepted; stock reservation and emails happen asynchronously.
        return ResponseEntity.accepted().location( URI.create( "/orders/" + order.id() ) ).body( OrderResponse.from( order ) );
    }

    @GetMapping( "/{id}" )
    ResponseEntity<OrderResponse> get( @PathVariable UUID id ) {
        return loadOrderUseCase.execute( new LoadOrderCommand( id ) )
                .map( o -> ResponseEntity.ok( OrderResponse.from( o ) ) )
                .orElse( ResponseEntity.notFound().build() );
    }

}
