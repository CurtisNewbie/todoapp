package com.curtisnewbie.util;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

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
    public static HBox expandingMargin() {
        HBox box = new HBox();
        HBox.setHgrow(box, Priority.SOMETIMES);
        return box;
    }

    /**
     * HBox acts as a "margin" never expand or shrinks
     */
    public static HBox fixedMargin(int width) {
        HBox box = new HBox();
        box.setMinWidth(width);
        HBox.setHgrow(box, Priority.NEVER);
        return box;
    }

    /**
     * Wrap a Node with padding, and the container that wraps it is returned (a HBox)
     *
     * @param node    node
     * @param padding Size of the padding
     * @return container that wraps the node
     */
    public static HBox wrapWithPadding(Node node, Insets padding) {
        HBox hbox = new HBox();
        hbox.getChildren().add(node);
        hbox.setPadding(padding);
        return hbox;
    }

    /**
     * Wrap a Node with common padding (see {@link #getCommonInsets()}), and the container that wraps it is returned (a
     * HBox)
     *
     * @param node node
     * @return container that wraps the node
     */
    public static HBox wrapWithCommonPadding(Node node) {
        return wrapWithPadding(node, getCommonInsets());
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
