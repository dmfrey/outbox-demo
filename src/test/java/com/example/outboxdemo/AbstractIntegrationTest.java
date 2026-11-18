package com.example.outboxdemo;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest( properties = "outbox.relay.poll-interval=200ms" )
@Import( TestcontainersConfiguration.class )
public abstract class AbstractIntegrationTest {

    @Autowired protected JdbcClient jdbc;
    @Autowired protected JsonMapper jsonMapper;
    @Autowired GenericContainer<?> mailpit;

    @BeforeEach
    void resetState() {
        jdbc.sql( "TRUNCATE outbox_message, processed_message, order_line, orders" ).update();
        jdbc.sql( "UPDATE stock SET available = CASE sku WHEN 'BOOK-1' THEN 10 WHEN 'MUG-1' THEN 5 ELSE 0 END" ).update();
        mailClient().delete().uri( "/api/v1/messages" ).retrieve().toBodilessEntity();
    }

    private RestClient mailClient() {
        return RestClient.create( "http://" + mailpit.getHost() + ":" + mailpit.getMappedPort( 8025 ) );
    }

    /** Subjects of every email Mailpit has received for the given recipient. */
    protected List<String> emailSubjectsFor( String recipient ) {
        var body = mailClient().get().uri( "/api/v1/messages" ).retrieve().body( String.class );
        var subjects = new ArrayList<String>();
        for ( JsonNode m : jsonMapper.readTree( body ).path( "messages" ) ) {
            for ( JsonNode to : m.path( "To" ) ) {
                if ( recipient.equals( to.path( "Address" ).asString() ) ) {
                    subjects.add( m.path( "Subject" ).asString() );
                }
            }
        }
        return subjects;
    }

}
