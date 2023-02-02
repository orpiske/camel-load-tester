package org.apache.camel.controller.test;

import org.apache.camel.controller.common.config.ConfigHolder;
import org.apache.camel.controller.test.routes.TestControllerRoutes;
import org.apache.camel.main.Main;

public class MainTestController {

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        final String configDir = ConfigHolder.getConfigurationDir();
        main.setPropertyPlaceholderLocations("file:" + configDir + "/application.properties");

        main.configure().addRoutesBuilder(new TestControllerRoutes());

        main.run();
    }
}
