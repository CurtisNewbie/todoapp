package com.curtisnewbie.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;

/**
 * <p>
 * Factory class to produce custom {@code Label}
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class LabelFactory {

    private LabelFactory() {
    }

    public static Label classicLabel(String name) {
        Label label = baseLabel(name);
        label.setPadding(new Insets(3, 2, 3, 2));
        return label;
    }

    public static Label leftPaddedLabel(String name) {
        Label label = baseLabel(name);
        label.setPadding(new Insets(3, 2, 3, 15));
        return label;
    }


    public static Label rightPaddedLabel(String name) {
        Label label = baseLabel(name);
        label.setPadding(new Insets(3, 15, 3, 2));
        return label;
    }

    private static Label baseLabel(String name) {
        Label label = new Label(name);
        label.setWrapText(false);
        label.setBorder(Border.EMPTY);
        label.setAlignment(Pos.BASELINE_CENTER);
        return label;
    }
}
