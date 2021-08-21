package com.curtisnewbie.util;

import com.curtisnewbie.App;
import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * Util for loading images
 *
 * @author yongjie.zhuang
 */
public final class ImageUtil {

    private static final String ICON_IMG_NAME = "icon.png";
    private static final String ARROW_LEFT_IMG_NAME = "arrowleft.png";
    private static final String ARROW_RIGHT_IMG_NAME = "arrowright.png";
    private static final String CLOSE_IMG_NAME = "close.png";

    public static final Image TITLE_ICON;
    public static final Image ARROW_LEFT_ICON;
    public static final Image ARROW_RIGHT_ICON;
    public static final Image CLOSE_ICON;

    static {
        TITLE_ICON = new Image(loadByName(ICON_IMG_NAME));
        ARROW_LEFT_ICON = new Image(loadByName(ARROW_LEFT_IMG_NAME));
        ARROW_RIGHT_ICON = new Image(loadByName(ARROW_RIGHT_IMG_NAME));
        CLOSE_ICON = new Image(loadByName(CLOSE_IMG_NAME));
    }

    private static InputStream loadByName(String name) {
        return App.class.getClassLoader().getResourceAsStream(name);
    }
}
