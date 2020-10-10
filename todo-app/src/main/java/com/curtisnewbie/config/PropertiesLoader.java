package com.curtisnewbie.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * <p>
 * Singleton that loads properties file. Use {@link #getInstance()} to retrieve the singleton instance.
 * </p>
 *
 * @author yongjie.zhuang
 */
public class PropertiesLoader {

    private static final String PROPERTIES_FILE = "application.properties";
    private static final PropertiesLoader INSTANCE = new PropertiesLoader();

    private final Properties properties;

    private PropertiesLoader() {
        this.properties = new Properties();
        try {
            this.properties
                    .load(new InputStreamReader(PropertiesLoader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE), "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get property
     *
     * @param key key
     * @return value (which may be null)
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get {@code PropertiesLoader} which is a singleton instance
     */
    public static PropertiesLoader getInstance() {
        return INSTANCE;
    }
}
