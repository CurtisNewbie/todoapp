package com.curtisnewbie.util;

import javafx.application.Platform;
import lombok.extern.slf4j.*;

import java.util.*;

/**
 * <p>
 * Util for JavaFX's application thread
 * </p>
 * <p>
 * This tool is only used for debugging
 * </p>
 *
 * @author yongjie.zhuang
 * @see RequiresFxThread
 */
@Slf4j
public final class FxThreadUtil {

    private static final String DEBUG = "debug";

    /**
     * This tool is only used for debugging, so this will always be false
     */
    private static final boolean isDebug;

    static {
        // java -Ddebug=true -jar ....
        final Properties properties = System.getProperties();
        isDebug = properties.containsKey(DEBUG) && Boolean.parseBoolean(properties.get(DEBUG).toString());
        if (isDebug)
            log.info("Debug mode turned on, will validate Fx Thread Confinement");
    }

    private FxThreadUtil() {
    }

    /**
     * Check if the current thread is FX's UI thread
     *
     * @throws ConcurrentModificationException if current thread is not FX's UI thread
     */
    public static void checkThreadConfinement() {
        if (!isDebug)
            return; // not debugging, do nothing

        if (!Platform.isFxApplicationThread()) {
            throw new ConcurrentModificationException("Method should only run inside UI thread, current thread: " + Thread.currentThread());
        }
    }
}
