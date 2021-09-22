package org.apache.camel.kafka.tester;

import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainProducer {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new MyProducer());
        main.run(args);
    }

}

