package com.curtisnewbie.util;

import com.curtisnewbie.App;
import javafx.scene.image.Image;

/**
 * Util for loading images
 *
 * @author yongjie.zhuang
 */
public final class ImageUtil {

    private static final String TITLE_ICON_NAME = "icon.png";

    public static final Image TITLE_ICON;

    static {
        TITLE_ICON = new Image(App.class.getClassLoader().getResourceAsStream(TITLE_ICON_NAME));
    }
}
