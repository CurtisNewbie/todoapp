package com.curtisnewbie.config;

import com.curtisnewbie.util.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.*;

/**
 * <p>
 * Singleton that loads properties file. Use {@link #getInstance()} to retrieve the singleton instance.
 * </p>
 * <p>
 * The {@link #changeToLocale(Locale)} must be called at least once before the {@link #getLocalizedProperty(String)} is invoked
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class PropertiesLoader {

    private static final String BASE_BUNDLE_NAME = "text";
    private static final String COMMON_PROPERTIES = "application.properties";
    private static final PropertiesLoader INSTANCE = new PropertiesLoader();

    private final Properties commonProp = new Properties();
    private final AtomicReference<ResourceBundle> localizedPropBundleRef = new AtomicReference<>();

    /** cache of ResourceBundle */
    @LockedBy(name = "this")
    private final HashMap<Locale, ResourceBundle> resourceBundleCache = new HashMap<>();

    private PropertiesLoader() {
        try (final InputStreamReader isr = read(COMMON_PROPERTIES)) {
            this.commonProp.load(isr);
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
            try (final InputStreamReader isr = read(BASE_BUNDLE_NAME + "_" + locale.getLanguage() + ".properties")) {
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

    private static InputStreamReader read(final String p) {
        return new InputStreamReader(requireNonNull(PropertiesLoader.class.getClassLoader().getResourceAsStream(p)), StandardCharsets.UTF_8);
    }
}
