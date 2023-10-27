package org.apache.camel.load.tester;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServer;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServerConfiguration;
import org.apache.camel.load.tester.common.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Camel Java DSL Router
 */
public class PlatformHTTPConsumerRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(PlatformHTTPConsumerRoute.class);
    private final VertxPlatformHttpServer vertxPlatformHttpServer;

    public PlatformHTTPConsumerRoute() {
        final int port = Parameters.httpPortConsumer();

        VertxPlatformHttpServerConfiguration conf = new VertxPlatformHttpServerConfiguration();
        conf.setBindPort(port);

        vertxPlatformHttpServer = new VertxPlatformHttpServer(conf);
    }

    public void kill(Exchange exchange) {
        LOG.info("Killing the process");

        try {
            vertxPlatformHttpServer.shutdown();
        } catch (Exception e) {
            LOG.warn("Failed to stop the server during shutdown: {}", e.getMessage(), e);
        }

        System.exit(0);
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() throws Exception {

        getCamelContext().addService(vertxPlatformHttpServer);

        from("platform-http:/hello")
            .transform(simple("Hello ${body}"));

        from("platform-http:/kill")
                .transform(simple("Killing the service"))
                .process(this::kill);
    }

}
