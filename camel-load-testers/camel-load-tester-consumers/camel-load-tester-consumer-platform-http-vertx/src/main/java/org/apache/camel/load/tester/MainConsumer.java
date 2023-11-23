package org.apache.camel.load.tester;

import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainConsumer {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();

        main.configure().addRoutesBuilder(new PlatformHTTPConsumerRoute());
        main.run(args);
    }



}

