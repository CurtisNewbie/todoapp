package com.curtisnewbie.util;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * <p>
 * Factory class for creating {@link Text}
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class TextFactory {

    private TextFactory() {
    }

    public static Text getClassicText(String txt) {
        Text text = new Text(txt);
        text.setTextOrigin(VPos.BOTTOM);
        text.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        return text;
    }

    public static HBox wrapWithPadding(Text txt, Insets padding) {
        HBox hbox = new HBox();
        hbox.getChildren().add(txt);
        hbox.setPadding(padding);
        return hbox;
    }

    public static HBox wrapWithCommonPadding(Text txt) {
        return wrapWithPadding(txt, getCommonInsets());
    }

    public static HBox getClassicTextWithPadding(String txt) {
        return wrapWithPadding(getClassicText(txt), getCommonInsets());
    }

    public static Insets getCommonInsets() {
        return new Insets(3, 2, 3, 2);
    }
}
