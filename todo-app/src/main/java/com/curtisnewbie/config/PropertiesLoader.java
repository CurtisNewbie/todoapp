package com.curtisnewbie.config;

import com.curtisnewbie.util.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

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

    @LockedBy(name = "this")
    private volatile ResourceBundle localizedProp;
    @LockedBy(name = "this")
    private volatile Locale locale;

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
            try (final InputStreamReader isr = read(BASE_BUNDLE_NAME + "_" + locale.getLanguage() + ".properties")) {
                this.localizedProp = new PropertyResourceBundle(isr);
                this.locale = locale;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public Locale getLocale() {
        return this.locale;
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

    private static InputStreamReader read(final String p) {
        return new InputStreamReader(requireNonNull(PropertiesLoader.class.getClassLoader().getResourceAsStream(p)), StandardCharsets.UTF_8);
    }
}
