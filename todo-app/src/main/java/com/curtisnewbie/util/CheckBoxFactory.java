package com.curtisnewbie.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;

/**
 * <p>
 * Factory for creating custom {@code CheckBox}
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class CheckBoxFactory {

    private CheckBoxFactory() {
    }

    public static CheckBox getClassicCheckBox() {
        CheckBox cb = new CheckBox();
        cb.setAlignment(Pos.CENTER_RIGHT);
        cb.setPadding(new Insets(1, 2, 1, 4));
        return cb;
    }
}
