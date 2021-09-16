package org.apache.camel.kafka.tester;

import org.apache.camel.builder.RouteBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;


/**
 * A Camel Java DSL Router
 */
public class MyProducer extends RouteBuilder {
    private static int count = 1;

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message using XPath
        from("timer:load?period=1&fixedRate=true")
            .process(new Processor(){
                public void process(Exchange e) {
                    count++;
                    LocalDateTime ldt = LocalDateTime.now();

                    String date = ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                    String body = String.format("[%s] %d -> test", date, count);
                    e.getMessage().setBody(body);
                }
            })
            .to("kafka:test");
    }

}
