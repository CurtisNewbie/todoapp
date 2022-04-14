package com.curtisnewbie.util;

import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.layout.*;

/**
 * <p>
 * Util class facilitate creation of margin (or something similar)
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class MarginFactory {

    private MarginFactory() {

    }

    /**
     * HBox acts as an infinite "margin" that expand "sometimes" when it's possible
     */
    public static HBox fillingMargin() {
        HBox box = new HBox();
        HBox.setHgrow(box, Priority.SOMETIMES);
        return box;
    }

    /**
     * HBox acts as a "margin" never expand or shrinks
     */
    public static HBox margin(int width) {
        HBox box = new HBox();
        box.setMinWidth(width);
        HBox.setHgrow(box, Priority.NEVER);
        return box;
    }

    /**
     * Wrap a Node with padding, and the container that wraps it is returned
     *
     * @param node node
     * @param w    width
     * @return container that wraps the node
     */
    public static Parent padding(Node node, int w) {
        return padding(node, w, w, w, w);
    }

    /**
     * Wrap a Node with padding, and the container that wraps it is returned
     *
     * @param node   node
     * @param top    top
     * @param right  right
     * @param bottom bottom
     * @return container that wraps the node
     */
    public static Parent padding(Node node, int top, int right, int bottom, int left) {
        return padding(node, new Insets(top, right, bottom, left));
    }

    /**
     * Wrap a Node with padding, and the container that wraps it is returned
     *
     * @param node    node
     * @param padding Size of the padding
     * @return container that wraps the node
     */
    public static Parent padding(Node node, Insets padding) {
        HBox p = new HBox();
        p.getChildren().add(node);
        p.setPadding(padding);
        return p;
    }

    /**
     * Wrap a Node with common padding (see {@link #getCommonInsets()}), and the container that wraps it is returned
     *
     * @param node node
     * @return container that wraps the node
     */
    public static Parent wrapWithCommonPadding(Node node) {
        return padding(node, getCommonInsets());
    }

    /**
     * Default Insets for convenience, which is of: top-3, right-2, bottom-3, left-2
     *
     * @return insets
     */
    public static Insets getCommonInsets() {
        return new Insets(3, 2, 3, 2);
    }
}
