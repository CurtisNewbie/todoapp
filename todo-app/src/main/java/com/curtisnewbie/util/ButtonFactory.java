package com.curtisnewbie.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;

/**
 * Factroy for buttons
 *
 * @author yongjie.zhuang
 */
public final class ButtonFactory {

    private ButtonFactory() {

    }

    public static final Button getRectBtn() {
        Button btn = new Button();
        btn.setAlignment(Pos.BASELINE_CENTER);
        btn.setShape(new Rectangle());
        return btn;
    }

    public static final Button getRectBtn(String txt) {
        var btn = getRectBtn();
        btn.setText(txt);
        return btn;
    }
}
