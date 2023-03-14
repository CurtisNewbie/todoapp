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

    private FxThreadUtil() {
    }

    /**
     * Check if the current thread is FX's UI thread
     *
     * @throws ConcurrentModificationException if current thread is not FX's UI thread
     */
    public static void checkThreadConfinement() {
        if (!DebugUtil.isDebug)
            return; // not debugging, do nothing

        if (!Platform.isFxApplicationThread()) {
            throw new ConcurrentModificationException("Method should only run inside UI thread, current thread: " + Thread.currentThread());
        }
    }
}
