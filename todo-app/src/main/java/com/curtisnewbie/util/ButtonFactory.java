package com.curtisnewbie.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;


/**
 * Factory for buttons
 *
 * @author yongjie.zhuang
 */
public final class ButtonFactory {
    private static final int DEF_IMAGE_SIDE_LEN = 22;

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

    public static final Button getArrowLeftBtn() {
        var btn = getRectBtn();
        var imgView = new ImageView(ImageUtil.ARROW_LEFT_ICON);
        imgView.setFitHeight(DEF_IMAGE_SIDE_LEN);
        imgView.setFitWidth(DEF_IMAGE_SIDE_LEN);
        btn.setGraphic(imgView);
        return btn;
    }

    public static final Button getArrowRightBtn() {
        var btn = getRectBtn();
        var imgView = new ImageView(ImageUtil.ARROW_RIGHT_ICON);
        imgView.setFitHeight(DEF_IMAGE_SIDE_LEN);
        imgView.setFitWidth(DEF_IMAGE_SIDE_LEN);
        btn.setGraphic(imgView);
        return btn;
    }

    public static final Button getCloseBtn() {
        var btn = getRectBtn();
        var imgView = new ImageView(ImageUtil.CLOSE_ICON);
        imgView.setFitHeight(DEF_IMAGE_SIDE_LEN);
        imgView.setFitWidth(DEF_IMAGE_SIDE_LEN);
        btn.setGraphic(imgView);
        return btn;
    }
}
