package com.curtisnewbie.util;

import com.curtisnewbie.controller.TodoJobView;
import javafx.application.Platform;

import java.util.ConcurrentModificationException;

/**
 * <p>
 * Util for JavaFX's application thread
 * </p>
 *
 * @author yongjie.zhuang
 * @see FxThreadConfinement
 */
public final class FxThreadUtil {

    private FxThreadUtil() {
    }

    /**
     * Check if the current thread is FX's UI thread
     *
     * @throws ConcurrentModificationException if current thread is not FX's UI thread
     */
    public static void checkThreadConfinement() {
        if (!Platform.isFxApplicationThread())
            throw new ConcurrentModificationException(TodoJobView.class.getName() + " should only be used inside UI thread");
    }
}
