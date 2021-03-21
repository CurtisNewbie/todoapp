package com.curtisnewbie.util;

import com.curtisnewbie.App;
import javafx.scene.image.Image;

/**
 * Util for loading images
 *
 * @author yongjie.zhuang
 */
public final class ImageUtil {

    private static final String ICON_IMG_NAME = "icon.png";
    private static final String ARROW_LEFT_IMG_NAME = "arrowleft.png";
    private static final String ARROW_RIGHT_IMG_NAME = "arrowright.png";

    public static final Image TITLE_ICON;
    public static final Image ARROW_LEFT_ICON;
    public static final Image ARROW_RIGHT_ICON;

    static {
        TITLE_ICON = new Image(App.class.getClassLoader().getResourceAsStream(ICON_IMG_NAME));
        ARROW_LEFT_ICON = new Image(App.class.getClassLoader().getResourceAsStream(ARROW_LEFT_IMG_NAME));
        ARROW_RIGHT_ICON = new Image(App.class.getClassLoader().getResourceAsStream(ARROW_RIGHT_IMG_NAME));
    }
}
