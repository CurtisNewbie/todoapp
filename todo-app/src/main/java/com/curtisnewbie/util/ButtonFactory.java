package com.curtisnewbie.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import static com.curtisnewbie.util.ImageUtil.*;


/**
 * Factory for buttons
 *
 * @author yongjie.zhuang
 */
public final class ButtonFactory {
    private static final int DEF_IMAGE_SIDE_LEN = 22;

    private ButtonFactory() {

    }

    public static Button getRectBtn() {
        Button btn = new Button();
        btn.setAlignment(Pos.BASELINE_CENTER);
        btn.setShape(new Rectangle());
        return btn;
    }

    public static Button getRectBtn(String txt) {
        Button btn = getRectBtn();
        btn.setText(txt);
        return btn;
    }

    public static Button getArrowLeftBtn() {
        Button btn = getRectBtn();
        ImageView imgView = new ImageView(loadFromCache(ARROW_LEFT_IMG_NAME));
        imgView.setFitHeight(DEF_IMAGE_SIDE_LEN);
        imgView.setFitWidth(DEF_IMAGE_SIDE_LEN);
        btn.setGraphic(imgView);
        return btn;
    }

    public static Button getArrowRightBtn() {
        Button btn = getRectBtn();
        ImageView imgView = new ImageView(loadFromCache(ARROW_RIGHT_IMG_NAME));
        imgView.setFitHeight(DEF_IMAGE_SIDE_LEN);
        imgView.setFitWidth(DEF_IMAGE_SIDE_LEN);
        btn.setGraphic(imgView);
        return btn;
    }

    public static Button getCloseBtn() {
        Button btn = getRectBtn();
        ImageView imgView = new ImageView(loadFromCache(CLOSE_IMG_NAME));
        imgView.setFitHeight(DEF_IMAGE_SIDE_LEN);
        imgView.setFitWidth(DEF_IMAGE_SIDE_LEN);
        btn.setGraphic(imgView);
        return btn;
    }
}
