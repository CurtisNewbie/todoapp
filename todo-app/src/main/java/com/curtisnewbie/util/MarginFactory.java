package com.curtisnewbie.util;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * @author yongjie.zhuang
 */
public class MarginFactory {

    /**
     * HBox acts as an infinit "margin" that expand "sometimes" when it's possible
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
}
