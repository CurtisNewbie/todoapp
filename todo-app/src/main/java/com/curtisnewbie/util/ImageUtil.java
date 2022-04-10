package com.curtisnewbie.util;

import com.curtisnewbie.App;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Util for loading images
 *
 * @author yongjie.zhuang
 */
public final class ImageUtil {

    public static final String ICON_IMG_NAME = "icon.png";
    public static final String ARROW_LEFT_IMG_NAME = "arrowleft.png";
    public static final String ARROW_RIGHT_IMG_NAME = "arrowright.png";
    public static final String CLOSE_IMG_NAME = "close.png";

    private static final Map<String /* name */, Image> imageCache = new HashMap<>();

    /**
     * Load an Image
     */
    public static Image load(String name) {
        return new Image(loadInputStream(name));
    }

    /** Load Image from cache */
    public static Image loadFromCache(String name) {
        synchronized (imageCache) {
            Image img;
            if ((img = imageCache.get(name)) != null)
                return img;
            img = load(name);
            imageCache.put(name, img);
            return img;
        }
    }

    private static InputStream loadInputStream(String name) {
        return App.class.getClassLoader().getResourceAsStream(name);
    }
}
