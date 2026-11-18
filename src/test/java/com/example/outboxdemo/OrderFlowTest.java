package com.example.outboxdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** LESSON 2: the whole chain, end to end, through a real Postgres + RabbitMQ + SMTP server. */
@AutoConfigureMockMvc
class OrderFlowTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void orderInStockIsConfirmedAndCustomerGetsTwoEmails() throws Exception {
        var id = placeOrder( "alice@example.com", "BOOK-1", 2 );

        await().atMost( Duration.ofSeconds( 20 ) ).untilAsserted( () -> mvc.perform( get( "/orders/" + id ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.status" ).value( "CONFIRMED" ) ) );

        await().atMost( Duration.ofSeconds( 20 ) ).untilAsserted( () ->
                assertThat( emailSubjectsFor( "alice@example.com" ) ).hasSize( 2 )
                        .anyMatch( s -> s.startsWith( "We received your order" ) )
                        .anyMatch( s -> s.endsWith( "is confirmed" ) ) );

        assertThat( jdbc.sql( "SELECT available FROM stock WHERE sku = 'BOOK-1'" ).query( Integer.class ).single() ).isEqualTo( 8 );
        await().untilAsserted( () -> assertThat( jdbc.sql( "SELECT count(*) FROM event_publication WHERE completion_date IS NULL" )
                .query( Integer.class ).single() ).isZero() );
    }

    @Test
    void orderOutOfStockIsRejectedAndNothingIsReserved() throws Exception {
        var id = placeOrder( "bob@example.com", "MUG-1", 99 );

        await().atMost( Duration.ofSeconds( 20 ) ).untilAsserted( () -> mvc.perform( get( "/orders/" + id ) )
                .andExpect( jsonPath( "$.status" ).value( "REJECTED" ) ) );

        await().atMost( Duration.ofSeconds( 20 ) ).untilAsserted( () ->
                assertThat( emailSubjectsFor( "bob@example.com" ) ).anyMatch( s -> s.endsWith( "is backordered" ) ) );
        assertThat( jdbc.sql( "SELECT available FROM stock WHERE sku = 'MUG-1'" ).query( Integer.class ).single() ).isEqualTo( 5 );
    }

    private String placeOrder( String email, String sku, int qty ) throws Exception {
        var body = mvc.perform( post( "/orders" ).contentType( MediaType.APPLICATION_JSON ).content( """
                        {"customerEmail":"%s","lines":[{"sku":"%s","quantity":%d}]}""".formatted( email, sku, qty ) ) )
                .andExpect( status().isAccepted() )
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree( body ).path( "id" ).asString();
    }

}
