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
public class LabelFactory {

    private LabelFactory() {
    }

    public static Label getClassicLabel(String name) {
        Label label = new Label(name);
        label.setPadding(new Insets(1, 2, 1, 2));
        label.setBorder(Border.EMPTY);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    public static Label getLeftPaddedLabel(String name) {
        Label label = new Label(name);
        label.setPadding(new Insets(1, 2, 1, 15));
        label.setBorder(Border.EMPTY);
        label.setAlignment(Pos.CENTER);
        return label;
    }
}
