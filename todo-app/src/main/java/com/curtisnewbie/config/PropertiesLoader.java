package com.curtisnewbie.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * <p>
 * Singleton that loads properties file. Use {@link #getInstance()} to retrieve the singleton instance.
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class PropertiesLoader {

    private static final String COMMON_PROPERTIES = "application.properties";
    private static final PropertiesLoader INSTANCE = new PropertiesLoader();

    private Properties commonProp = new Properties();
    private ResourceBundle localizedProp;

    private PropertiesLoader() {
        try {
            this.commonProp.load(
                    new InputStreamReader(PropertiesLoader.class.getClassLoader().getResourceAsStream(COMMON_PROPERTIES),
                            "UTF-8")
            );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Load Resource bundle
     *
     * @param bundleBaseName bundle's base name
     * @param locale         locale to be used
     */
    public void loadResourceBundle(String bundleBaseName, Locale locale) {
        this.localizedProp = ResourceBundle.getBundle(bundleBaseName, locale);
    }

    /**
     * Get common property
     *
     * @param key key
     * @return value (which may be null)
     */
    public String getCommonProperty(String key) {
        return commonProp.getProperty(key);
    }

    /**
     * Get localized property
     *
     * @param key key for language-related properties
     * @return value (which may be null)
     */
    public String getLocalizedProperty(String key) {
        return localizedProp.getString(key);
    }

    /**
     * Get {@code PropertiesLoader} which is a singleton instance
     */
    public static PropertiesLoader getInstance() {
        return INSTANCE;
    }
}
