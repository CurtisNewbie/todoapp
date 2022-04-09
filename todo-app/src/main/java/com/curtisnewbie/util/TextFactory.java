package com.curtisnewbie.util;

import javafx.geometry.VPos;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import static com.curtisnewbie.util.MarginFactory.getCommonInsets;
import static com.curtisnewbie.util.MarginFactory.wrapWithPadding;

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

    public static TextArea selectableText(String txt) {
        TextArea tf = new TextArea(txt);
        tf.setEditable(false);
        tf.setWrapText(true);
        tf.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-padding: 0;");
        return tf;
    }

    public static HBox selectableTextWithPadding(String txt) {
        return wrapWithPadding(selectableText(txt), getCommonInsets());
    }

    public static Text getClassicText(String txt) {
        Text text = new Text(txt);
        text.setTextOrigin(VPos.BOTTOM);
        text.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        return text;
    }

    public static HBox getClassicTextWithPadding(String txt) {
        return wrapWithPadding(getClassicText(txt), getCommonInsets());
    }

}
