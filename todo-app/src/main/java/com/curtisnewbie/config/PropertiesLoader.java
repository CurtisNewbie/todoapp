package com.curtisnewbie.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Singleton that loads properties file. Use {@link #getInstance()} to retrieve the singleton instance.
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class PropertiesLoader {

    private static final String BASE_BUNDLE_NAME = "text";
    private static final String COMMON_PROPERTIES = "application.properties";
    private static final PropertiesLoader INSTANCE = new PropertiesLoader();

    private Properties commonProp = new Properties();
    private final AtomicReference<ResourceBundle> localizedPropBundleRef = new AtomicReference<>();

    /** cache of ResourceBundle, thread-safe, and it is guarded by a lock for 'this' */
    private final ConcurrentMap<Locale, ResourceBundle> resourceBundleCache = new ConcurrentHashMap<>();

    private PropertiesLoader() {
        try {
            this.commonProp.load(
                    new InputStreamReader(PropertiesLoader.class.getClassLoader().getResourceAsStream(COMMON_PROPERTIES),
                            "UTF-8")
            );
            changeToLocale(Locale.ENGLISH); // by default english
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Reload resource bundle, change to the specified locale
     *
     * @param locale locale to be used
     */
    public void changeToLocale(Locale locale) {
        synchronized (this) {
            ResourceBundle resourceBundle = resourceBundleCache.get(locale);
            if (resourceBundle != null) {
                this.localizedPropBundleRef.set(resourceBundle);
                return;
            }

            // this is to avoid encoding issue
            final String fname = BASE_BUNDLE_NAME + "_" + locale.getLanguage() + ".properties";
            try (final InputStreamReader isr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(fname), "UTF-8");) {
                resourceBundle = new PropertyResourceBundle(isr);
                this.resourceBundleCache.put(locale, resourceBundle);
                this.localizedPropBundleRef.set(resourceBundle);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
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
        return localizedPropBundleRef.get().getString(key);
    }

    /**
     * Get {@code PropertiesLoader} which is a singleton instance
     */
    public static PropertiesLoader getInstance() {
        return INSTANCE;
    }

}
