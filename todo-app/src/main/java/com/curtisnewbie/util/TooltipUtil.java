package com.curtisnewbie.util;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * Util for Tooltip
 *
 * @author yongj.zhuang
 */
public final class TooltipUtil {

    public static void tooltip(Node node, String msg) {
        Tooltip.install(node, new Tooltip(msg));
    }
}
