package com.curtisnewbie.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
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
     * Get property
     *
     * @param keySeg array of strings that will be concatenated as a single key
     * @return value (which may be null)
     */
    public String get(String... keySeg) {
        if (keySeg.length == 0)
            return null;
        StringBuilder sb = new StringBuilder();
        Arrays.stream(keySeg).forEach(e -> sb.append(e));
        return properties.getProperty(sb.toString());
    }

    /**
     * Get property
     *
     * @param key  key for language-related properties
     * @param lang language
     * @return value (which may be null)
     */
    public String get(String key, Language lang) {
        return get(key, lang.key);
    }

    /**
     * Get {@code PropertiesLoader} which is a singleton instance
     */
    public static PropertiesLoader getInstance() {
        return INSTANCE;
    }
}
