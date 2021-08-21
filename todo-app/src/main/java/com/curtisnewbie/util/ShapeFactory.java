package com.curtisnewbie.util;

import javafx.scene.effect.Bloom;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Factory for shapes
 *
 * @author yongjie.zhuang
 */
public final class ShapeFactory {

    public static final Color CUSTOM_GREEN_COLOR = Color.web("#7DA948");
//    public static final Color CUSTOM_RED_COLOR = Color.web("#E75839");
    private static final int POINT_RADIUS = 3;

    public static Circle greenCircle() {
        return coloredCircle(CUSTOM_GREEN_COLOR);
    }

    public static Circle redCircle() {
        return coloredCircle(Color.RED);
    }

    public static Circle orangeCircle() {
        return coloredCircle(Color.ORANGE);
    }

    public static Circle coloredCircle(Color color) {
        Circle p = defaultCircle();
        p.setFill(color);
        return p;
    }

    public static Circle defaultCircle() {
        Circle p = new Circle();
        p.setRadius(POINT_RADIUS);
        p.setSmooth(true);
        p.setEffect(new Bloom());
        return p;
    }
}
