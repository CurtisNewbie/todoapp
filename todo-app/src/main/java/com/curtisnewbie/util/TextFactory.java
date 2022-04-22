package com.curtisnewbie.util;

import javafx.geometry.VPos;
import javafx.scene.*;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import static com.curtisnewbie.util.MarginFactory.getCommonInsets;
import static com.curtisnewbie.util.MarginFactory.padding;

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
        tf.setPrefRowCount(20);
        tf.setEditable(false);
        tf.setWrapText(true);
        tf.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-padding: 0;");
        return tf;
    }

    public static Parent selectableTextWithPadding(String txt) {
        return padding(selectableText(txt), getCommonInsets());
    }

    public static Text getClassicText(String txt) {
        Text text = new Text(txt);
        text.setTextOrigin(VPos.BOTTOM);
        text.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        return text;
    }

    public static Parent getClassicTextWithPadding(String txt) {
        return padding(getClassicText(txt), getCommonInsets());
    }

}
