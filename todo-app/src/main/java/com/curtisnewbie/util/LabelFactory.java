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
        Label label = getBaseLabel(name);
        label.setPadding(new Insets(2, 2, 2, 2));
        return label;
    }

    public static Label getLeftPaddedLabel(String name) {
        Label label = getBaseLabel(name);
        label.setPadding(new Insets(2, 2, 2, 15));
        return label;
    }

    private static Label getBaseLabel(String name) {
        Label label = new Label(name);
        label.setWrapText(true);
        label.setBorder(Border.EMPTY);
        label.setAlignment(Pos.CENTER);
        return label;
    }
}
