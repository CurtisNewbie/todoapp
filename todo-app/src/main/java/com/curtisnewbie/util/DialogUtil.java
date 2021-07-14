package com.curtisnewbie.util;


import javafx.scene.control.Dialog;

import java.util.Objects;

/**
 * Util class for {@link javafx.scene.control.Dialog}
 *
 * @author yongjie.zhuang
 */
public final class DialogUtil {

    private DialogUtil() {
    }

    /**
     * Disable header in given dialog
     */
    public static void disableHeader(Dialog<?> dialog) {
        Objects.requireNonNull(dialog);
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
    }
}
