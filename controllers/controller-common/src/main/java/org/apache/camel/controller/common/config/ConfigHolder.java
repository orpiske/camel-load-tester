package org.apache.camel.controller.common.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public final class ConfigHolder {
    private static ConfigHolder configHolder;
    private Properties properties;

    public ConfigHolder() throws IOException {
        final String configDir = System.getProperty("config.dir");

        properties = new Properties();

        try (FileInputStream f = new FileInputStream(configDir + "/application.properties")) {
            properties.load(f);
        }
    }

    public static ConfigHolder getInstance() {
        if (configHolder == null) {
            try {
                configHolder = new ConfigHolder();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configHolder;
    }

    public static String getConfigurationDir() {
        return System.getProperty("config.dir");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public Object get(Object key) {
        return properties.get(key);
    }

    public Object getOrDefault(Object key, Object defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }
}
