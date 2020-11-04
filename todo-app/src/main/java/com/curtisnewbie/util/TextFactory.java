package com.curtisnewbie.util;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
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
