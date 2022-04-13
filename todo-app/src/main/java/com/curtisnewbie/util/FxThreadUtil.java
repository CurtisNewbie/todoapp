package com.curtisnewbie.util;

import javafx.application.Platform;

import java.util.ConcurrentModificationException;

/**
 * <p>
 * Util for JavaFX's application thread
 * </p>
 * <p>
 * This tool is only used for debugging
 * </p>
 *
 * @author yongjie.zhuang
 * @see FxThreadConfinement
 */
public final class FxThreadUtil {

    /**
     * This tool is only used for debugging, so this will always be false
     */
    private static final boolean isDebug = false;

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
